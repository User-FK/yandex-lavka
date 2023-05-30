package ru.yandex.yandexlavka.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CompleteOrderRequest(
        @JsonProperty(value = "complete_info", required = true) List<CompleteOrderDto> completeInfo
) {
}
