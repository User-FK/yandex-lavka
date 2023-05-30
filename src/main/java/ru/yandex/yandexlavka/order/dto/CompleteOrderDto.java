package ru.yandex.yandexlavka.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record CompleteOrderDto(
        @JsonProperty(value = "courier_id", required = true) Long courierId,
        @JsonProperty(value = "order_id", required = true) Long orderId,
        @JsonProperty(value = "complete_time", required = true) LocalDateTime completeTime
) {
}
