package com.gym.crm.actuator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseHealthIndicatorTest {
    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private DatabaseMetaData metaData;

    @InjectMocks
    private DatabaseHealthIndicator indicator;

    @Test
    void health_shouldReturnUp_whenConnectionValid() throws SQLException {
        Status expectedStatus = Status.UP;
        String expectedDatabase = "MySQL";
        String expectedConnectionStatus = "Connection successful";

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(anyInt())).thenReturn(true);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn("MySQL");

        Health actual = indicator.health();

        Status actualStatus = actual.getStatus();
        Map<String, Object> actualDetails = actual.getDetails();
        assertThat(actualStatus).isEqualTo(expectedStatus);
        assertThat(actualDetails).containsEntry("database", expectedDatabase);
        assertThat(actualDetails).containsEntry("status", expectedConnectionStatus);
        verify(connection, times(1)).close();
    }

    @Test
    void health_shouldReturnDown_whenConnectionInvalid() throws SQLException {
        Status expectedStatus = Status.DOWN;
        String expectedDatabase = "MySQL";
        String expectedConnectionStatus = "Connection invalid";

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(anyInt())).thenReturn(false);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn("MySQL");

        Health actual = indicator.health();

        Status actualStatus = actual.getStatus();
        Map<String, Object> actualDetails = actual.getDetails();
        assertThat(actualStatus).isEqualTo(expectedStatus);
        assertThat(actualDetails).containsEntry("database", expectedDatabase);
        assertThat(actualDetails).containsEntry("status", expectedConnectionStatus);
        verify(connection, times(1)).close();
    }

    @Test
    void health_shouldReturnDown_whenSQLException() throws SQLException {
        String expectedError = "Database Connection Error";
        Status expectedStatus = Status.DOWN;

        when(dataSource.getConnection()).thenThrow(new SQLException(expectedError));

        Health actual = indicator.health();

        Status actualStatus = actual.getStatus();
        Map<String, Object> actualDetails = actual.getDetails();
        assertThat(actualStatus).isEqualTo(expectedStatus);
        assertThat(actualDetails).containsEntry("error", expectedError);
    }
}