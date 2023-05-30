package ru.yandex.yandexlavka.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.yandex.yandexlavka.courier.db.CourierEntity;
import ru.yandex.yandexlavka.courier.db.CourierRepository;
import ru.yandex.yandexlavka.courier.dto.CourierDto;
import ru.yandex.yandexlavka.order.db.OrderEntity;
import ru.yandex.yandexlavka.order.db.OrderRepository;
import ru.yandex.yandexlavka.order.dto.OrderDto;
import ru.yandex.yandexlavka.service.AssignService;
import ru.yandex.yandexlavka.service.SimpleAssignService;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

@Configuration
@EnableJpaRepositories(basePackages = {
        "ru.yandex.yandexlavka.courier.db",
        "ru.yandex.yandexlavka.order.db"})
public class ApplicationConfig implements WebMvcConfigurer {
    @Bean
    public AssignService assignService(CourierRepository courierRepository,
                                       OrderRepository orderRepository,
                                       @Qualifier("courierEntityDtoMapper") Function<CourierEntity, CourierDto> courierEntityDtoMapper,
                                       @Qualifier("orderEntityDtoMapper") Function<OrderEntity, OrderDto> orderEntityDtoMapper) {
        var service = new SimpleAssignService(orderRepository,
                courierRepository,
                courierEntityDtoMapper,
                orderEntityDtoMapper);

        return service;
    }

    @Bean
    public ConcurrentMap<String, PathRateLimiter> requestLimiters() {
        return new ConcurrentHashMap<>();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateInterceptor(requestLimiters())).addPathPatterns("/**");
    }
}
