package ru.yandex.yandexlavka.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CourierGroupOrder(@JsonProperty("courier_id") long courierId,
                                @JsonProperty("orders") List<GroupOrders> orders) {
}
