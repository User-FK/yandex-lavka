package ru.yandex.yandexlavka.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CreateOrderRequest(@JsonProperty(value = "orders", required = true) List<CreateOrderDto> orders) {
}
