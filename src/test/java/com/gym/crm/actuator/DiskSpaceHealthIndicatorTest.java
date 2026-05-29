package com.gym.crm.actuator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class DiskSpaceHealthIndicatorTest {
    @Spy
    private DiskSpaceHealthIndicator indicator;

    @Test
    void health_shouldReturnUp_whenEnoughDiskSpace() {
        long freeSpace = 200L * 1024 * 1024;
        Status expectedStatus = Status.UP;

        doReturn(freeSpace).when(indicator).getFreeSpace();

        Health actual = indicator.health();

        Status actualStatus = actual.getStatus();
        Map<String, Object> actualDetails = actual.getDetails();
        assertThat(actualStatus).isEqualTo(expectedStatus);
        assertThat(actualDetails).containsEntry("free_memory_bytes", freeSpace);
    }

    @Test
    void health_shouldReturnDown_whenLowDiskSpace() {
        long freeSpace = 50L * 1024 * 1024;
        Status expectedStatus = Status.DOWN;
        String expectedMessage = "Low disk space";

        doReturn(freeSpace).when(indicator).getFreeSpace();

        Health actual = indicator.health();

        Status actualStatus = actual.getStatus();
        Map<String, Object> actualDetails = actual.getDetails();
        assertThat(actualStatus).isEqualTo(expectedStatus);
        assertThat(actualDetails).containsEntry("message", expectedMessage);
    }
}