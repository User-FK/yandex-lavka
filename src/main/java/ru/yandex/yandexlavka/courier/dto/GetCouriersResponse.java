package ru.yandex.yandexlavka.courier.dto;

import java.util.List;

public record GetCouriersResponse(List<CourierDto> couriers,
                                  Long limit,
                                  Long offset) {
}
