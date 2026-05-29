package com.gym.crm.actuator.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginMetricsTest {
    private MeterRegistry registry;
    private LoginMetrics metrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metrics = new LoginMetrics(registry);
    }

    @Test
    void incrementCount_shouldIncrementSuccessCounter_whenSuccess() {
        metrics.incrementCount(true);

        Counter actual = registry.find("gym.auth.login.attempts")
                .tag("status", "success")
                .counter();

        assertThat(actual).isNotNull();
        assertThat(actual.count()).isEqualTo(1.0);
    }

    @Test
    void incrementCount_shouldIncrementFailureCounter_whenFailure() {
        metrics.incrementCount(false);

        Counter actual = registry.find("gym.auth.login.attempts")
                .tag("status", "failure")
                .counter();

        assertThat(actual).isNotNull();
        assertThat(actual.count()).isEqualTo(1.0);
    }
}