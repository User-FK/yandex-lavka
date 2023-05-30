package ru.yandex.yandexlavka.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GroupOrders(@JsonProperty("group_id") Long groupId,
                          @JsonProperty("orders") List<OrderDto> orders) {
}
