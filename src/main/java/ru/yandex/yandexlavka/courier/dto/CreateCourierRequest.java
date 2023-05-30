package ru.yandex.yandexlavka.courier.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CreateCourierRequest(
        @JsonProperty(value = "couriers", required = true) List<CreateCourierDto> couriers
) {
}
