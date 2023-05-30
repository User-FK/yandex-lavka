package ru.yandex.yandexlavka.service;

import ru.yandex.yandexlavka.courier.dto.CourierDto;
import ru.yandex.yandexlavka.courier.dto.CreateCourierDto;
import ru.yandex.yandexlavka.order.dto.CompleteOrderDto;
import ru.yandex.yandexlavka.order.dto.CourierGroupOrder;
import ru.yandex.yandexlavka.order.dto.CreateOrderDto;
import ru.yandex.yandexlavka.order.dto.OrderDto;

import java.time.LocalDate;
import java.util.List;

public interface AssignService {
    // assign existing orders to existing couriers
    List<CourierGroupOrder> assign();

    List<CourierGroupOrder> getAssignments(Long courierId, LocalDate date);

    List<OrderDto> complete(List<CompleteOrderDto> toComplete);
}
