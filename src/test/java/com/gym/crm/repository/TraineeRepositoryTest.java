package com.gym.crm.repository;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DatabaseSetup("/dataset/trainee-dataset.xml")
class TraineeRepositoryTest extends BaseTestRepository<TraineeRepository> {
    @Test
    void findByUser_Username_eagerlyLoadsTrainers_withAllFields() {
        Optional<Trainee> actual = repository.findByUser_Username("Julia.Tomas");

        assertThat(actual).isPresent();
        Set<Trainer> trainers = actual.get().getTrainers();
        assertThat(trainers).hasSize(1);

        Trainer trainer = trainers.iterator().next();

        assertThat(trainer.getUser()).isNotNull();
        assertThat(trainer.getUser().getFirstName()).isEqualTo("Tom");
        assertThat(trainer.getUser().getLastName()).isEqualTo("Trainer");
        assertThat(trainer.getUser().getUsername()).isEqualTo("Tom.Trainer");
        assertThat(trainer.getUser().getIsActive()).isTrue();
        assertThat(trainer.getSpecialization()).isNotNull();
        assertThat(trainer.getSpecialization().getTrainingTypeName()).isEqualTo("Cardio");
    }

    @Test
    void findByUser_Username_eagerlyLoadsTrainings_withAllFields() {
        Optional<Trainee> actual = repository.findByUser_Username("Julia.Tomas");

        assertThat(actual).isPresent();
        Set<Training> trainings = actual.get().getTrainings();
        assertThat(trainings).hasSize(1);

        Training training = trainings.iterator().next();
        assertThat(training.getId()).isEqualTo(1L);
        assertThat(training.getTrainingName()).isEqualTo("Morning Cardio");
        assertThat(training.getTrainingDate()).isEqualTo(LocalDate.of(2024, 5, 10));
        assertThat(training.getTrainingDuration()).isEqualTo(60);

        assertThat(training.getTrainer().getUser().getUsername()).isEqualTo("Tom.Trainer");

        assertThat(training.getTrainingType().getId()).isEqualTo(1L);
        assertThat(training.getTrainingType().getTrainingTypeName()).isEqualTo("Cardio");
    }

    @Test
    void findByUser_Username_returnsTrainee_whenUsernameExists() {
        Optional<Trainee> actual = repository.findByUser_Username("Julia.Tomas");

        assertThat(actual).isPresent();
        assertThat(actual.get().getUser().getUsername()).isEqualTo("Julia.Tomas");
        assertThat(actual.get().getUser().getFirstName()).isEqualTo("Julia");
        assertThat(actual.get().getUser().getLastName()).isEqualTo("Tomas");
        assertThat(actual.get().getUser().getIsActive()).isTrue();
        assertThat(actual.get().getDateOfBirth()).isEqualTo("2000-03-10");
        assertThat(actual.get().getAddress()).isEqualTo("10 Sheep St");
    }

    @Test
    void findByUser_Username_eagerlyLoadsUserAndTrainers() {
        Optional<Trainee> actual = repository.findByUser_Username("Julia.Tomas");

        assertThat(actual).isPresent();
        assertThat(actual.get().getUser()).isNotNull();
        assertThat(actual.get().getTrainers()).hasSize(1);
        assertThat(actual.get().getTrainers().iterator().next().getUser().getUsername()).isEqualTo("Tom.Trainer");
    }

    @Test
    void findByUser_Username_returnsEmpty_whenUsernameNotFound() {
        Optional<Trainee> actual = repository.findByUser_Username("ghost");

        assertThat(actual).isEmpty();
    }

    @Test
    void existsByUser_Username_returnsTrue_whenExists() {
        boolean actual = repository.existsByUser_Username("Ellis.Hargrove");

        assertThat(actual).isTrue();
    }

    @Test
    void existsByUser_Username_returnsFalse_whenNotExists() {
        boolean actual = repository.existsByUser_Username("nobody");

        assertThat(actual).isFalse();
    }
}