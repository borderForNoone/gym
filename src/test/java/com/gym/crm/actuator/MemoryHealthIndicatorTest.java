package com.gym.crm.actuator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class MemoryHealthIndicatorTest {
    @Spy
    private MemoryHealthIndicator indicator;

    @Test
    void health_shouldReturnUp_whenUsageBelowThreshold() {
        Status expectedStatus = Status.UP;
        String expectedUsagePercentage = "10.00%";

        doReturn(1000L).when(indicator).getMaxMemory();
        doReturn(900L).when(indicator).getTotalMemory();
        doReturn(800L).when(indicator).getFreeMemory();

        Health actual = indicator.health();

        Status actualStatus = actual.getStatus();
        Map<String, Object> actualDetails = actual.getDetails();
        assertThat(actualStatus).isEqualTo(expectedStatus);
        assertThat(actualDetails.keySet())
                .contains("free_memory_bytes")
                .contains("allocated_memory_bytes")
                .contains("max_memory_bytes")
                .contains("used_memory_bytes");
        assertThat(actualDetails).containsEntry("usage_percentage", expectedUsagePercentage);
    }

    @ParameterizedTest
    @ValueSource(longs = {900L, 950L})
    void health_shouldReturnDown_whenUsageAtOrExceedsThreshold(long totalMemory) {
        Status expectedStatus = Status.DOWN;
        String expectedMessage = "Memory threshold exceeded";

        doReturn(1000L).when(indicator).getMaxMemory();
        doReturn(totalMemory).when(indicator).getTotalMemory();
        doReturn(0L).when(indicator).getFreeMemory();

        Health actual = indicator.health();

        Status actualStatus = actual.getStatus();
        Map<String, Object> actualDetails = actual.getDetails();
        assertThat(actualStatus).isEqualTo(expectedStatus);
        assertThat(actualDetails).containsEntry("message", expectedMessage);
    }
}