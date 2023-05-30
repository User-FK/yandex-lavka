package ru.yandex.yandexlavka;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.yandexlavka.courier.CourierService;
import ru.yandex.yandexlavka.courier.CourierUtils;
import ru.yandex.yandexlavka.courier.db.CourierEntity;
import ru.yandex.yandexlavka.courier.db.CourierRepository;
import ru.yandex.yandexlavka.courier.dto.CreateCourierDto;
import ru.yandex.yandexlavka.order.OrderService;
import ru.yandex.yandexlavka.order.db.OrderEntity;
import ru.yandex.yandexlavka.order.db.OrderRepository;
import ru.yandex.yandexlavka.order.dto.CompleteOrderDto;
import ru.yandex.yandexlavka.order.dto.CreateOrderDto;
import ru.yandex.yandexlavka.service.AssignService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AssignmentServiceTest extends IntegrationEnvironment {
    @Autowired
    CourierRepository courierRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    AssignService assignService;

    @Autowired
    OrderService orderService;

    @Autowired
    CourierService courierService;

    @Test
    void cannotAcceptTest() {
        var couriers = new ArrayList<CreateCourierDto>();
        couriers.add(new CreateCourierDto("FOOT", List.of(1L, 2L, 3L), List.of("01:00-23:00")));

        courierService.addCouriers(couriers);

        var orders = new ArrayList<CreateOrderDto>();
        orders.add(new CreateOrderDto(100500D, 52L, List.of("01:00-23:00"), 1L));

        orderService.addOrders(orders);

        assignService.assign();

        var ordersEntity = orderRepository.findAll();
        var couriersEntity = courierRepository.findAll();

        assertTrue(ordersEntity.size() != 0);
        assertTrue(couriersEntity.size() != 0);

        for (var order: ordersEntity)
            assertEquals(OrderEntity.Status.NEW, order.getStatus());

        for (var courier: couriersEntity)
            assertEquals(CourierEntity.Status.FREE, courier.getStatus());
    }

    @Test
    void autoAcceptedTest() {
        var couriers = new ArrayList<CreateCourierDto>();
        couriers.add(new CreateCourierDto("FOOT", List.of(1L, 2L, 3L), List.of("01:00-23:00")));
        couriers.add(new CreateCourierDto("AUTO", List.of(1L, 2L, 3L), List.of("01:00-23:00")));

        courierService.addCouriers(couriers);

        var orders = new ArrayList<CreateOrderDto>();
        orders.add(new CreateOrderDto(CourierUtils.getMaxWeight("AUTO"), 52L, List.of("01:00-23:00"), 1L));

        orderService.addOrders(orders);

        assignService.assign();

        var courierOptional = courierRepository.findAll().stream()
                .filter(courier -> courier.getCourierType().equals("AUTO") && courier.getStatus().equals(CourierEntity.Status.BUSY))
                .findFirst();

        assertFalse(courierOptional.isEmpty());

        var orderOptional = orderRepository.findAll().stream()
                .filter(order -> order.getStatus().equals(OrderEntity.Status.IN_PROGRESS))
                .findFirst();

        assertFalse(orderOptional.isEmpty());
    }

    @Test
    void completeTest() {
        var couriers = new ArrayList<CreateCourierDto>();
        couriers.add(new CreateCourierDto("FOOT", List.of(1L, 2L, 3L), List.of("01:00-23:00")));
        couriers.add(new CreateCourierDto("AUTO", List.of(1L, 2L, 3L), List.of("01:00-23:00")));

        var orders = new ArrayList<CreateOrderDto>();
        orders.add(new CreateOrderDto(CourierUtils.getMaxWeight("AUTO"), 52L, List.of("01:00-23:00"), 1L));

        courierService.addCouriers(couriers);
        orderService.addOrders(orders);
        assignService.assign();

        var courier = courierRepository.findAll().stream()
                .filter(x -> x.getCourierType().equals("AUTO") && x.getStatus().equals(CourierEntity.Status.BUSY))
                .findFirst()
                .get();

        var order = orderRepository.findAll().stream()
                .filter(x -> x.getStatus().equals(OrderEntity.Status.IN_PROGRESS))
                .findFirst()
                .get();

        assignService.complete(List.of(new CompleteOrderDto(courier.getId(), order.getId(), LocalDateTime.now())));

        order = orderRepository.findById(order.getId()).get();
        courier = courierRepository.findById(courier.getId()).get();

        assertEquals(CourierEntity.Status.FREE, courier.getStatus());
        assertEquals(OrderEntity.Status.COMPLETED, order.getStatus());
    }

    @Test
    void metaInfoTest() {
        var couriers = new ArrayList<CreateCourierDto>();
        couriers.add(new CreateCourierDto("FOOT", List.of(1L, 2L, 3L), List.of("01:00-23:00")));
        couriers.add(new CreateCourierDto("AUTO", List.of(1L, 2L, 3L), List.of("01:00-23:00")));

        var orders = new ArrayList<CreateOrderDto>();
        orders.add(new CreateOrderDto(CourierUtils.getMaxWeight("AUTO"), 52L, List.of("01:00-23:00"), 1L));

        courierService.addCouriers(couriers);
        orderService.addOrders(orders);
        assignService.assign();

        var courier = courierRepository.findAll().stream()
                .filter(x -> x.getCourierType().equals("AUTO") && x.getStatus().equals(CourierEntity.Status.BUSY))
                .findFirst()
                .get();

        var order = orderRepository.findAll().stream()
                .filter(x -> x.getStatus().equals(OrderEntity.Status.IN_PROGRESS))
                .findFirst()
                .get();

        assignService.complete(List.of(new CompleteOrderDto(courier.getId(), order.getId(), LocalDateTime.parse("2000-01-02T00:00:00"))));

        order = orderRepository.findById(order.getId()).get();
        courier = courierRepository.findById(courier.getId()).get();

        double targetEarnings = 4 * 1;
        double targetRating = 1d / 24; // 24 -- hours between dates

        assertEquals(CourierEntity.Status.FREE, courier.getStatus());
        assertEquals(OrderEntity.Status.COMPLETED, order.getStatus());

        var metaInfo = courierService.getCouriersMetaInfo(courier.getId(), LocalDate.parse("2000-01-01"), LocalDate.parse("2000-01-02")).get();

        assertEquals(targetEarnings, metaInfo.earnings());
        assertEquals(targetRating, metaInfo.rating());
    }
}
