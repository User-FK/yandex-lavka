package ru.yandex.yandexlavka.courier;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.yandexlavka.courier.db.CourierEntity;
import ru.yandex.yandexlavka.courier.db.Region;
import ru.yandex.yandexlavka.courier.db.WorkTime;
import ru.yandex.yandexlavka.courier.dto.CourierDto;

import java.util.function.Function;

@Configuration
public class CourierConfig {
    @Bean
    public Function<CourierEntity, CourierDto> courierEntityDtoMapper() {
        return courierEntity -> new CourierDto(
                courierEntity.getId(),
                courierEntity.getCourierType(),
                courierEntity.getRegions().stream().map(Region::getId).toList(),
                courierEntity.getWorkTimes().stream().map(WorkTime::toString).toList());
    }
}
