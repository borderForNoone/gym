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
class DiskSpaceHealthIndicatorTest {
    @Spy
    private DiskSpaceHealthIndicator indicator;

    @Test
    void health_shouldReturnUp_whenEnoughDiskSpace() {
        doReturn(200L * 1024 * 1024).when(indicator).getFreeSpace();

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("free_memory_bytes", 200L * 1024 * 1024);
    }

    @Test
    void health_shouldReturnDown_whenLowDiskSpace() {
        doReturn(50L * 1024 * 1024).when(indicator).getFreeSpace();

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("message", "Low disk space");
    }
}