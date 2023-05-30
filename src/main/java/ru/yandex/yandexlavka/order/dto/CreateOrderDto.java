package ru.yandex.yandexlavka.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CreateOrderDto(
        @JsonProperty(value = "weight", required = true) Double weight,
        @JsonProperty(value = "regions", required = true) Long region,
        @JsonProperty(value = "delivery_hours", required = true) List<String> deliveryHours,
        @JsonProperty(value = "cost", required = true) Long cost
) {
}
