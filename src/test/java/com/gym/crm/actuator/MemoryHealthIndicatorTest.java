package com.gym.crm.actuator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class MemoryHealthIndicatorTest {

    @Spy
    private MemoryHealthIndicator indicator;

    @Test
    void health_shouldReturnUp_whenUsageBelowThreshold() {
        doReturn(1000L).when(indicator).getMaxMemory();
        doReturn(900L).when(indicator).getTotalMemory();
        doReturn(800L).when(indicator).getFreeMemory();

        Health actual = indicator.health();

        assertThat(actual.getStatus()).isEqualTo(Status.UP);
        assertThat(actual.getDetails().keySet())
                .contains("free_memory_bytes")
                .contains("allocated_memory_bytes")
                .contains("max_memory_bytes")
                .contains("used_memory_bytes");
        assertThat(actual.getDetails()).containsEntry("usage_percentage", "10.00%");
    }

    @Test
    void health_shouldReturnDown_whenUsageEqualToThreshold() {
        doReturn(1000L).when(indicator).getMaxMemory();
        doReturn(900L).when(indicator).getTotalMemory();
        doReturn(0L).when(indicator).getFreeMemory();

        Health actual = indicator.health();

        assertThat(actual.getStatus()).isEqualTo(Status.DOWN);
        assertThat(actual.getDetails()).containsEntry("message", "Memory threshold exceeded");
    }

    @Test
    void health_shouldReturnDown_whenUsageExceedsThreshold() {
        doReturn(1000L).when(indicator).getMaxMemory();
        doReturn(950L).when(indicator).getTotalMemory();
        doReturn(0L).when(indicator).getFreeMemory();

        Health actual = indicator.health();

        assertThat(actual.getStatus()).isEqualTo(Status.DOWN);
        assertThat(actual.getDetails()).containsEntry("message", "Memory threshold exceeded");
    }
}