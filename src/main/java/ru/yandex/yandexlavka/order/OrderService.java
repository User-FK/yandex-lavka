package ru.yandex.yandexlavka.order;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.yandex.yandexlavka.order.db.DeliveryHoursEntity;
import ru.yandex.yandexlavka.order.db.OrderEntity;
import ru.yandex.yandexlavka.order.db.OrderRepository;
import ru.yandex.yandexlavka.order.dto.CreateOrderDto;
import ru.yandex.yandexlavka.order.dto.OrderDto;
import ru.yandex.yandexlavka.repository.ChunkRequest;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final Function<OrderEntity, OrderDto> orderEntityDtoMapper;
    private final OrderMapper orderMapper = new OrderMapper();

    public OrderService(OrderRepository orderRepository, Function<OrderEntity, OrderDto> orderEntityDtoMapper) {
        this.orderRepository = orderRepository;
        this.orderEntityDtoMapper = orderEntityDtoMapper;
    }

    public List<OrderDto> addOrders(List<CreateOrderDto> ordersDto) {
        List<OrderEntity> orders = ordersDto.stream().map(orderMapper).collect(Collectors.toCollection(ArrayList<OrderEntity>::new)); // WARNING: should collect to modifiable list
        orders = orderRepository.saveAll(orders); // now orders have corresponding id-s

        return orders.stream().map(orderEntityDtoMapper).toList();
    }

    public List<OrderDto> getOrders(Long limit, Long offset) {
        //return orderRepository.findAll().stream().map(orderEntityDtoMapper).toList();
        return orderRepository.findAll(new ChunkRequest(offset.intValue(), limit.intValue(), Sort.by("id")))
                .stream().map(orderEntityDtoMapper).toList();
    }

    public Optional<OrderDto> getSingleOrder(Long orderId) {
        return orderRepository.findById(orderId).map(orderEntityDtoMapper);
    }

    private static class OrderMapper implements Function<CreateOrderDto, OrderEntity> {
        @Override
        public OrderEntity apply(CreateOrderDto orderDto) {
            var order = new OrderEntity();

            order.setWeight(orderDto.weight());
            order.setRegion(orderDto.region());
            order.setCost(orderDto.cost());
            order.setStatus(OrderEntity.Status.NEW);

            for (String timeString: orderDto.deliveryHours()) {
                var times = timeString.split("-");
                var start = LocalTime.parse(times[0]);
                var end = LocalTime.parse(times[1]);

                order.getDeliveryHours().add(new DeliveryHoursEntity(start, end));
            }

            return order;
        }
    }
}
