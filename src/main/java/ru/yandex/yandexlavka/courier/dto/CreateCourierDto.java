package ru.yandex.yandexlavka.courier.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CreateCourierDto(
        @JsonProperty(value = "courier_type", required = true) String courierType,
        @JsonProperty(value = "regions", required = true) List<Long> regions,
        @JsonProperty(value = "working_hours", required = true) List<String> workingHours
) {
}
