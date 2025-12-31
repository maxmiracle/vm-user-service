package ru.maximserver.vmuserservice.config;

import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import org.jooq.DSLContext;
import org.jooq.conf.RenderNameCase;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.jooq.SQLDialect.POSTGRES;

@Configuration
public class JooqConfig {

    @Bean(name = "postgresConnectionFactory")
    public PostgresqlConnectionFactory connectionFactory(
            @Value("${spring.r2dbc.properties.host}")
            String host,
            @Value("${spring.r2dbc.properties.port}")
            int port,
            @Value("${spring.r2dbc.password}")
            String password,
            @Value("${spring.r2dbc.username}")
            String username,
            @Value("${spring.r2dbc.properties.database}")
            String database
    ) {
        return new PostgresqlConnectionFactory(PostgresqlConnectionConfiguration.builder()
                .host(host)
                .port(port)
                .username(username)
                .database(database)
                .password(password)
                .build()
        );
    }

    @Bean
    public DSLContext dsl(@Qualifier("postgresConnectionFactory") ConnectionFactory connectionFactory) {
        Settings settings = new Settings();
        settings.setRenderNameCase(RenderNameCase.LOWER);
        return DSL.using(connectionFactory, POSTGRES, settings);
    }
}
