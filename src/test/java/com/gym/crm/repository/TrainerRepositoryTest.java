package com.gym.crm.repository;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.gym.crm.model.Trainer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DatabaseSetup("/dataset/trainer.xml")
class TrainerRepositoryTest extends BaseTestRepository<TrainerRepository> {
    private static final String USERNAME = "Callum.Whitfield";
    private static final String SECOND_USERNAME = "Nora.Pemberton";

    @Test
    void findByUser_Username_returnsTrainer_whenExists() {
        Optional<Trainer> result = repository.findByUser_Username(USERNAME);

        assertThat(result).isPresent();

        Trainer trainer = result.get();

        assertThat(trainer.getUser().getUsername()).isEqualTo("Callum.Whitfield");
        assertThat(trainer.getUser().getFirstName()).isEqualTo("Callum");
        assertThat(trainer.getUser().getLastName()).isEqualTo("Whitfield");
        assertThat(trainer.getUser().getPassword()).isEqualTo("pass111");
        assertThat(trainer.getUser().getIsActive()).isTrue();

        assertThat(trainer.getSpecialization()).isNotNull();
        assertThat(trainer.getSpecialization().getTrainingTypeName()).isEqualTo("Yoga");
    }

    @Test
    void findByUser_Username_returnsEmpty_whenNotFound() {
        Optional<Trainer> result = repository.findByUser_Username("ghost");

        assertThat(result).isEmpty();
    }

    @Test
    void existsByUser_Username_returnsTrue_whenExists() {
        boolean actual = repository.existsByUser_Username(USERNAME);

        assertThat(actual).isTrue();
    }

    @Test
    void existsByUser_Username_returnsFalse_whenNotExists() {
        boolean actual = repository.existsByUser_Username("nobody");

        assertThat(actual).isFalse();
    }

    @Test
    void findByUser_UsernameNotIn_excludesGivenUsernames() {
        List<Trainer> result = repository.findByUser_UsernameNotIn(List.of(USERNAME));

        assertThat(result).hasSize(1);

        Trainer trainer = result.getFirst();
        assertThat(trainer.getUser().getUsername()).isEqualTo("Nora.Pemberton");
        assertThat(trainer.getUser().getFirstName()).isEqualTo("Nora");
        assertThat(trainer.getUser().getLastName()).isEqualTo("Pemberton");
        assertThat(trainer.getUser().getPassword()).isEqualTo("pass222");
        assertThat(trainer.getUser().getIsActive()).isTrue();
        assertThat(trainer.getSpecialization().getTrainingTypeName()).isEqualTo("Pilates");
    }

    @Test
    void findByUser_UsernameNotIn_returnsAll_whenNoUsernameMatches() {
        List<Trainer> result = repository.findByUser_UsernameNotIn(List.of("nonexistent.user"));

        assertThat(result).hasSize(2);

        assertThat(result).anySatisfy(trainer -> {
            assertThat(trainer.getUser().getUsername()).isEqualTo("Callum.Whitfield");
            assertThat(trainer.getUser().getFirstName()).isEqualTo("Callum");
            assertThat(trainer.getUser().getLastName()).isEqualTo("Whitfield");
            assertThat(trainer.getSpecialization().getTrainingTypeName()).isEqualTo("Yoga");
        });

        assertThat(result).anySatisfy(trainer -> {
            assertThat(trainer.getUser().getUsername()).isEqualTo("Nora.Pemberton");
            assertThat(trainer.getUser().getFirstName()).isEqualTo("Nora");
            assertThat(trainer.getUser().getLastName()).isEqualTo("Pemberton");
            assertThat(trainer.getSpecialization().getTrainingTypeName()).isEqualTo("Pilates");
        });
    }

    @Test
    void findByUser_UsernameIn_returnsMatchingTrainers() {
        List<Trainer> result = repository.findByUser_UsernameIn(List.of(USERNAME, SECOND_USERNAME));

        assertThat(result).hasSize(2);

        assertThat(result).anySatisfy(trainer -> {
            assertThat(trainer.getUser().getUsername()).isEqualTo("Callum.Whitfield");
            assertThat(trainer.getUser().getFirstName()).isEqualTo("Callum");
            assertThat(trainer.getSpecialization().getTrainingTypeName()).isEqualTo("Yoga");
        });
        assertThat(result).anySatisfy(trainer -> {
            assertThat(trainer.getUser().getUsername()).isEqualTo("Nora.Pemberton");
            assertThat(trainer.getUser().getFirstName()).isEqualTo("Nora");
            assertThat(trainer.getSpecialization().getTrainingTypeName()).isEqualTo("Pilates");
        });
    }

    @Test
    void findByUser_UsernameIn_returnsEmpty_whenNoneMatch() {
        List<Trainer> result = repository.findByUser_UsernameIn(List.of("nobody"));

        assertThat(result).isEmpty();
    }

    @Test
    void findByIdNotIn_excludesGivenIds() {
        Trainer callum = repository.findByUser_Username(USERNAME).orElseThrow();
        List<Trainer> result = repository.findByIdNotIn(List.of(callum.getId()));

        assertThat(result).hasSize(1);

        Trainer trainer = result.getFirst();
        assertThat(trainer.getId()).isNotEqualTo(callum.getId());
        assertThat(trainer.getUser().getUsername()).isEqualTo("Nora.Pemberton");
        assertThat(trainer.getUser().getFirstName()).isEqualTo("Nora");
        assertThat(trainer.getUser().getLastName()).isEqualTo("Pemberton");
        assertThat(trainer.getSpecialization().getTrainingTypeName()).isEqualTo("Pilates");
    }

    @Test
    void findByIdNotIn_returnsAll_whenNoIdMatches() {
        List<Trainer> result = repository.findByIdNotIn(List.of(-1L));

        assertThat(result).hasSize(2);

        assertThat(result).anySatisfy(trainer -> {
            assertThat(trainer.getUser().getUsername()).isEqualTo("Callum.Whitfield");
            assertThat(trainer.getSpecialization().getTrainingTypeName()).isEqualTo("Yoga");
        });
        assertThat(result).anySatisfy(trainer -> {
            assertThat(trainer.getUser().getUsername()).isEqualTo("Nora.Pemberton");
            assertThat(trainer.getSpecialization().getTrainingTypeName()).isEqualTo("Pilates");
        });
    }
}