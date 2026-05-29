package com.gym.crm.actuator.metrics;

import com.gym.crm.repository.TraineeRepository;
import com.gym.crm.repository.TrainerRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class GaugeMetrics {
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    public GaugeMetrics(MeterRegistry registry, TraineeRepository traineeRepository, TrainerRepository trainerRepository) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;

        Gauge.builder("gym.users.active", this, GaugeMetrics::getActiveTraineeCount)
                .description("Number of currently active users")
                .tag("type", "trainee")
                .register(registry);

        Gauge.builder("gym.users.active", this, GaugeMetrics::getActiveTrainerCount)
                .description("Number of currently active users")
                .tag("type", "trainer")
                .register(registry);

        Gauge.builder("gym.users.total", traineeRepository, TraineeRepository::count)
                .description("Total number of users")
                .tag("type", "trainee")
                .register(registry);

        Gauge.builder("gym.users.total", trainerRepository, TrainerRepository::count)
                .description("Total number of users")
                .tag("type", "trainer")
                .register(registry);
    }

    private double getActiveTraineeCount() {
        return traineeRepository.countByUserIsActive(true);
    }

    private double getActiveTrainerCount() {
        return trainerRepository.countByUserIsActive(true);
    }
}