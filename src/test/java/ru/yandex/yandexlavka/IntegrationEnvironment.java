package ru.yandex.yandexlavka;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

public class IntegrationEnvironment {
    public static PostgreSQLContainer POSTGRES_CONTAINER = new PostgreSQLContainer("postgres:15.2-alpine");

    static {
        POSTGRES_CONTAINER.withExposedPorts(5432);
        POSTGRES_CONTAINER.start();

        var postgresDataSource = DataSourceBuilder
                .create()
                .url(POSTGRES_CONTAINER.getJdbcUrl())
                .username(POSTGRES_CONTAINER.getUsername())
                .password(POSTGRES_CONTAINER.getPassword())
                .build();
    }

    @DynamicPropertySource
    public static void setupDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        registry.add("spring.datasource.port", () -> POSTGRES_CONTAINER.getMappedPort(5432));
    }
}
