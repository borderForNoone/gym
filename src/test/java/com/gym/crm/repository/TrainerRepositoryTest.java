package com.gym.crm.repository;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.gym.crm.model.Trainer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DatabaseSetup("/dataset/trainer.xml")
class TrainerRepositoryTest extends BaseTestRepository<TrainerRepository> {
    @Test
    void findByUser_Username_returnsTrainer_whenExists() {
        Optional<Trainer> result = repository.findByUser_Username("Callum.Whitfield");

        assertThat(result).isPresent();
        assertThat(result.get().getUser().getFirstName()).isEqualTo("Callum");
    }

    @Test
    void findByUser_Username_returnsEmpty_whenNotFound() {
        assertThat(repository.findByUser_Username("ghost")).isEmpty();
    }

    @Test
    void existsByUser_Username_returnsTrue_whenExists() {
        assertThat(repository.existsByUser_Username("Callum.Whitfield")).isTrue();
    }

    @Test
    void existsByUser_Username_returnsFalse_whenNotExists() {
        assertThat(repository.existsByUser_Username("nobody")).isFalse();
    }

    @Test
    void findByUser_UsernameNotIn_excludesGivenUsernames() {
        List<Trainer> result = repository.findByUser_UsernameNotIn(List.of("Callum.Whitfield"));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUser().getUsername()).isEqualTo("Nora.Pemberton");
    }

    @Test
    void findByUser_UsernameNotIn_returnsAll_whenNoUsernameMatches() {
        List<Trainer> result = repository.findByUser_UsernameNotIn(List.of("nonexistent.user"));

        assertThat(result).extracting(t -> t.getUser().getUsername()).containsExactlyInAnyOrder("Callum.Whitfield", "Nora.Pemberton");
    }

    @Test
    void findByUser_UsernameIn_returnsMatchingTrainers() {
        List<Trainer> result = repository.findByUser_UsernameIn(List.of("Callum.Whitfield", "Nora.Pemberton"));

        assertThat(result).extracting(t -> t.getUser().getUsername()).containsExactlyInAnyOrder("Callum.Whitfield", "Nora.Pemberton");
    }

    @Test
    void findByUser_UsernameIn_returnsEmpty_whenNoneMatch() {
        assertThat(repository.findByUser_UsernameIn(List.of("nobody"))).isEmpty();
    }

    @Test
    void findByIdNotIn_excludesGivenIds() {
        Trainer callum = repository.findByUser_Username("Callum.Whitfield").orElseThrow();

        List<Trainer> result = repository.findByIdNotIn(List.of(callum.getId()));

        assertThat(result).hasSize(1);
        assertThat(result).extracting(Trainer::getId).doesNotContain(callum.getId());
    }

    @Test
    void findByIdNotIn_returnsAll_whenNoIdMatches() {
        List<Trainer> result = repository.findByIdNotIn(List.of(-1L));

        assertThat(result).extracting(t -> t.getUser().getUsername()).containsExactlyInAnyOrder("Callum.Whitfield", "Nora.Pemberton");
    }
}