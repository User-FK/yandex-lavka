package ru.yandex.yandexlavka.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

public record OrderAssignResponse(@JsonProperty("date") LocalDate date,
                                  @JsonProperty("couriers") List<CourierGroupOrder> couriers) {
}
