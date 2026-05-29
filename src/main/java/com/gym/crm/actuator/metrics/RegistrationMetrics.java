package com.gym.crm.actuator.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class RegistrationMetrics {
    private final Counter traineeSuccessCounter;
    private final Counter trainerSuccessCounter;
    private final Counter traineeFailureCounter;
    private final Counter trainerFailureCounter;

    public RegistrationMetrics(MeterRegistry meterRegistry) {
        this.traineeSuccessCounter = createCounter(meterRegistry, "trainee", "success");
        this.trainerSuccessCounter = createCounter(meterRegistry, "trainer", "success");
        this.traineeFailureCounter = createCounter(meterRegistry, "trainee", "failure");
        this.trainerFailureCounter = createCounter(meterRegistry, "trainer", "failure");
    }

    public void incrementTraineeCount(boolean success) {
        if (success) {
            traineeSuccessCounter.increment();
        } else {
            traineeFailureCounter.increment();
        }
    }

    public void incrementTrainerCount(boolean success) {
        if (success) {
            trainerSuccessCounter.increment();
        } else {
            trainerFailureCounter.increment();
        }
    }

    private Counter createCounter(MeterRegistry registry, String type, String status) {
        return Counter.builder("gym.user.registrations")
                .description("Total number of registrations")
                .tag("type", type)
                .tag("status", status)
                .register(registry);
    }
}