package com.gym.crm.actuator.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TrainingMetrics {

    private final MeterRegistry meterRegistry;
    private final Map<String, Counter> counters = new HashMap<>();

    public TrainingMetrics(MeterRegistry registry) {
        this.meterRegistry = registry;
    }

    public void incrementCounter(String trainingType) {
        counters.computeIfAbsent(trainingType, this::createCounter).increment();
    }

    private Counter createCounter(String trainingType) {
        return Counter.builder("gym.training.creations")
                .description("Total number of trainings created")
                .tag("type", trainingType)
                .register(meterRegistry);
    }
}