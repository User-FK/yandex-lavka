package ru.yandex.yandexlavka.order;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.yandexlavka.order.db.DeliveryHoursEntity;
import ru.yandex.yandexlavka.order.db.OrderEntity;
import ru.yandex.yandexlavka.order.dto.OrderDto;

import java.util.function.Function;

@Configuration
public class OrderConfig {
    @Bean
    public Function<OrderEntity, OrderDto> orderEntityDtoMapper() {
        return orderEntity -> new OrderDto(
                orderEntity.getId(),
                orderEntity.getWeight(),
                orderEntity.getRegion(),
                orderEntity.getDeliveryHours().stream().map(DeliveryHoursEntity::toString).toList(),
                orderEntity.getCost(),
                orderEntity.getCompletedTime() == null ? null : orderEntity.getCompletedTime().toString()
        );
    }
}
