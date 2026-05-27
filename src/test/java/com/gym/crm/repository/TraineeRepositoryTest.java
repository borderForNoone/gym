package com.gym.crm.repository;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.gym.crm.model.Trainee;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DatabaseSetup("/dataset/trainee-dataset.xml")
class TraineeRepositoryTest extends BaseTestRepository<TraineeRepository> {
    @Test
    void findByUser_Username_returnsTrainee_whenUsernameExists() {
        Optional<Trainee> result = repository.findByUser_Username("Julia.Tomas");

        assertThat(result).isPresent();
        assertThat(result.get().getUser().getFirstName()).isEqualTo("Julia");
        assertThat(result.get().getUser().getLastName()).isEqualTo("Tomas");
    }

    @Test
    void findByUser_Username_eagerlyLoadsUserAndTrainers() {
        Optional<Trainee> result = repository.findByUser_Username("Julia.Tomas");

        assertThat(result).isPresent();
        assertThat(result.get().getUser()).isNotNull();
        assertThat(result.get().getUser().getUsername()).isEqualTo("Julia.Tomas");
        assertThat(result.get().getTrainers()).hasSize(1);
    }

    @Test
    void findByUser_Username_returnsEmpty_whenUsernameNotFound() {
        assertThat(repository.findByUser_Username("ghost")).isEmpty();
    }

    @Test
    void existsByUser_Username_returnsTrue_whenExists() {
        assertThat(repository.existsByUser_Username("Ellis.Hargrove")).isTrue();
    }

    @Test
    void existsByUser_Username_returnsFalse_whenNotExists() {
        assertThat(repository.existsByUser_Username("nobody")).isFalse();
    }
}