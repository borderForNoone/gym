package com.gym.crm.actuator.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegistrationMetricsTest {

    private MeterRegistry registry;
    private RegistrationMetrics metrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metrics = new RegistrationMetrics(registry);
    }

    @Test
    void incrementTraineeCount_shouldIncrementSuccessCounter_whenSuccess() {
        metrics.incrementTraineeCount(true);

        Counter actual = registry.find("gym.user.registrations")
                .tag("type", "trainee")
                .tag("status", "success")
                .counter();

        assertThat(actual).isNotNull();
        assertThat(actual.count()).isEqualTo(1.0);
    }

    @Test
    void incrementTraineeCount_shouldIncrementFailureCounter_whenFailure() {
        metrics.incrementTraineeCount(false);

        Counter actual = registry.find("gym.user.registrations")
                .tag("type", "trainee")
                .tag("status", "failure")
                .counter();

        assertThat(actual).isNotNull();
        assertThat(actual.count()).isEqualTo(1.0);
    }

    @Test
    void incrementTrainerCount_shouldIncrementSuccessCounter_whenSuccess() {
        metrics.incrementTrainerCount(true);

        Counter actual = registry.find("gym.user.registrations")
                .tag("type", "trainer")
                .tag("status", "success")
                .counter();

        assertThat(actual).isNotNull();
        assertThat(actual.count()).isEqualTo(1.0);
    }

    @Test
    void incrementTrainerCount_shouldIncrementFailureCounter_whenFailure() {
        metrics.incrementTrainerCount(false);

        Counter actual = registry.find("gym.user.registrations")
                .tag("type", "trainer")
                .tag("status", "failure")
                .counter();

        assertThat(actual).isNotNull();
        assertThat(actual.count()).isEqualTo(1.0);
    }
}