package ru.yandex.yandexlavka.order.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OrderDto(
        @JsonProperty(value = "order_id", required = true)
        Long orderId,

        @JsonProperty(value = "weight", required = true)
        Double weight,
        @JsonProperty(value = "regions", required = true)
        Long region,

        @JsonProperty(value = "delivery_hours", required = true)
        List<String> deliveryHours,
        @JsonProperty(value = "cost", required = true)
        Long cost,
        @JsonProperty(value = "completed_time")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String completedTime
) {
}
