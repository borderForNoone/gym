package com.gym.crm.actuator.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class LoginMetrics {
    private final Counter successCounter;
    private final Counter failureCounter;

    public LoginMetrics(MeterRegistry registry) {
        this.successCounter = createCounter(registry, "success");
        this.failureCounter = createCounter(registry, "failure");
    }

    public void incrementCount(boolean success) {
        if (success) {
            successCounter.increment();
        } else {
            failureCounter.increment();
        }
    }

    private Counter createCounter(MeterRegistry meterRegistry, String status) {
        return Counter.builder("gym.auth.login.attempts")
                .description("Total login attempts")
                .tag("status", status)
                .register(meterRegistry);
    }
}