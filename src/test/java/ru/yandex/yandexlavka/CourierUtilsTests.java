package ru.yandex.yandexlavka;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.yandex.yandexlavka.courier.CourierUtils;
import ru.yandex.yandexlavka.courier.db.CourierEntity;
import ru.yandex.yandexlavka.courier.db.WorkTime;
import ru.yandex.yandexlavka.order.db.DeliveryHoursEntity;
import ru.yandex.yandexlavka.order.db.OrderEntity;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Tests below should be independent on Spring's context

public class CourierUtilsTests {
    @Test
    void weightAcceptTest() {
        var order = new OrderEntity();
        order.setWeight(25d);
        order.setDeliveryHours(List.of(new DeliveryHoursEntity(LocalTime.of(0, 1), LocalTime.of(23, 0))));

        var courier = new CourierEntity();
        courier.setWorkTimes(List.of(new WorkTime(LocalTime.of(1, 1), LocalTime.of(20, 0))));

        courier.setCourierType("FOOT");
        Assertions.assertFalse(CourierUtils.canAccept(courier, order));

        courier.setCourierType("BIKE");
        assertFalse(CourierUtils.canAccept(courier, order));

        courier.setCourierType("AUTO");
        assertTrue(CourierUtils.canAccept(courier, order));
    }

    @Test
    void statusAcceptTest() {
        var order = new OrderEntity();
        order.setWeight(-1d);
        order.setDeliveryHours(List.of(new DeliveryHoursEntity(LocalTime.of(0, 1), LocalTime.of(23, 0))));

        var courier = new CourierEntity();
        courier.setWorkTimes(List.of(new WorkTime(LocalTime.of(1, 1), LocalTime.of(20, 0))));
        courier.setCourierType("AUTO");

        courier.setStatus(CourierEntity.Status.FREE);
        assertTrue(CourierUtils.canAccept(courier, order));

        courier.setStatus(CourierEntity.Status.BUSY);
        assertFalse(CourierUtils.canAccept(courier, order));
    }

    @Test
    void timesAcceptTest() {
        var order = new OrderEntity();
        var deliveryHours = order.getDeliveryHours();

        order.setWeight(0d);

        var courier = new CourierEntity();
        var workingHours = courier.getWorkTimes();

        courier.setCourierType("FOOT");

        assertFalse(CourierUtils.canAccept(courier, order));

        deliveryHours.add(new DeliveryHoursEntity(LocalTime.of(1, 0), LocalTime.of(5, 0)));
        workingHours.add(new WorkTime(LocalTime.of(14, 0), LocalTime.of(15, 0)));

        assertFalse(CourierUtils.canAccept(courier, order));

        workingHours.add(new WorkTime(LocalTime.of(0, 5), LocalTime.of(9, 0)));

        assertTrue(CourierUtils.canAccept(courier, order));
    }
}
