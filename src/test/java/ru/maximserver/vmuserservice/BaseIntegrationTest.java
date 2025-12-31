package ru.maximserver.vmuserservice;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
public class BaseIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"))
                    .waitingFor(Wait.forListeningPort())
                    .withCommand("postgres", "-c", "max_connections=500");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> postgreSQLContainer.getJdbcUrl().replaceFirst("jdbc:", "r2dbc:"));
        registry.add("spring.r2dbc.username", postgreSQLContainer::getUsername);
        registry.add("spring.r2dbc.password", postgreSQLContainer::getPassword);
        registry.add("spring.r2dbc.properties.schema", () -> "public");
        registry.add("spring.r2dbc.properties.database", postgreSQLContainer::getDatabaseName);
        registry.add("spring.r2dbc.properties.host", postgreSQLContainer::getHost);
        registry.add("spring.r2dbc.properties.port", postgreSQLContainer::getFirstMappedPort);

        registry.add("spring.liquibase.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.liquibase.user", postgreSQLContainer::getUsername);
        registry.add("spring.liquibase.password", postgreSQLContainer::getPassword);

        registry.add("spring.liquibase.clear-checksums", () -> "false");
    }
}
