package com.gym.crm.actuator;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
@RequiredArgsConstructor
public class DatabaseHealthIndicator implements HealthIndicator {
    private final DataSource dataSource;

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(1);

            if (valid) {
                return Health.up()
                        .withDetail("database", connection.getMetaData().getDatabaseProductName())
                        .withDetail("status", "Connection successful")
                        .build();
            }

            return Health.down()
                    .withDetail("database", connection.getMetaData().getDatabaseProductName())
                    .withDetail("status", "Connection invalid")
                    .build();
        } catch (SQLException e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}