package com.gym.crm.repository;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.gym.crm.facade.dto.TrainerInfoDTO;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DatabaseSetup("/dataset/trainer.xml")
@DatabaseTearDown(value = "/dataset/trainer.xml", type = DatabaseOperation.DELETE_ALL)
class TrainerRepositoryTest extends BaseTestRepository<TrainerRepository> {
    private static final String USERNAME = "Callum.Whitfield";
    private static final String SECOND_USERNAME = "Nora.Pemberton";

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_shouldUpdateIsActive_whenSetActiveCalledOnTrainer() {
        Trainer existing = repository.findByUser_Username(USERNAME).orElseThrow();
        Trainer updated = existing.toBuilder().user(existing.getUser().toBuilder().isActive(false).build()).build();

        repository.save(updated);

        assertThat(repository.findByUser_Username(USERNAME).orElseThrow().getUser().getIsActive()).isFalse();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_shouldUpdatePassword_whenChangePasswordCalledOnTrainer() {
        Trainer existing = repository.findByUser_Username(USERNAME).orElseThrow();
        Trainer updated = existing.toBuilder().user(existing.getUser().toBuilder().password("new_encoded_pass").build()).build();

        repository.save(updated);

        assertThat(repository.findByUser_Username(USERNAME).orElseThrow().getUser().getPassword()).isEqualTo("new_encoded_pass");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_shouldUpdateSpecialization_whenUpdateTrainerCalled() {
        Trainer existing = repository.findByUser_Username(USERNAME).orElseThrow();
        TrainingType pilates = trainingTypeRepository.findByTrainingTypeName("Pilates").orElseThrow();
        Trainer updated = existing.toBuilder().specialization(pilates).build();

        repository.save(updated);

        assertThat(repository.findByUser_Username(USERNAME).orElseThrow().getSpecialization().getTrainingTypeName()).isEqualTo("Pilates");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_shouldUpdateName_whenUpdateProfileCalledOnTrainer() {
        Trainer existing = repository.findByUser_Username(USERNAME).orElseThrow();
        Trainer updated = existing.toBuilder().user(existing.getUser().toBuilder().firstName("CallumUpdated").lastName("WhitfieldUpdated").build()).build();

        repository.save(updated);

        Trainer actual = repository.findByUser_Username(USERNAME).orElseThrow();
        assertThat(actual.getUser().getFirstName()).isEqualTo("CallumUpdated");
        assertThat(actual.getUser().getLastName()).isEqualTo("WhitfieldUpdated");
    }

    @Test
    void findByUser_Username_returnsTrainer_whenExists() {
        Optional<Trainer> actual = repository.findByUser_Username(USERNAME);

        assertThat(actual).isPresent();
        assertThat(actual.get().getUser().getUsername()).isEqualTo("Callum.Whitfield");
        assertThat(actual.get().getUser().getFirstName()).isEqualTo("Callum");
        assertThat(actual.get().getUser().getLastName()).isEqualTo("Whitfield");
        assertThat(actual.get().getUser().getPassword()).isEqualTo("pass111");
        assertThat(actual.get().getUser().getIsActive()).isTrue();
        assertThat(actual.get().getSpecialization().getTrainingTypeName()).isEqualTo("Yoga");
    }

    @Test
    void findByUser_Username_returnsEmpty_whenNotFound() {
        Optional<Trainer> actual = repository.findByUser_Username("ghost");

        assertThat(actual).isEmpty();
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
        List<Trainer> actual = repository.findByUser_UsernameNotIn(List.of(USERNAME));

        assertThat(actual).hasSize(1);
        assertThat(actual.getFirst().getUser().getUsername()).isEqualTo("Nora.Pemberton");
        assertThat(actual.getFirst().getUser().getFirstName()).isEqualTo("Nora");
        assertThat(actual.getFirst().getUser().getLastName()).isEqualTo("Pemberton");
        assertThat(actual.getFirst().getUser().getPassword()).isEqualTo("pass222");
        assertThat(actual.getFirst().getUser().getIsActive()).isTrue();
        assertThat(actual.getFirst().getSpecialization().getTrainingTypeName()).isEqualTo("Pilates");
    }

    @Test
    void findByUser_UsernameNotIn_returnsAll_whenNoUsernameMatches() {
        List<Trainer> actual = repository.findByUser_UsernameNotIn(List.of("nonexistent.user"));

        assertThat(actual).hasSize(2);
        assertThat(actual).extracting(trainer -> trainer.getUser().getUsername()).containsExactlyInAnyOrder("Callum.Whitfield", "Nora.Pemberton");
    }

    @Test
    void findByUser_UsernameIn_returnsMatchingTrainers() {
        List<Trainer> actual = repository.findByUser_UsernameIn(List.of(USERNAME, SECOND_USERNAME));

        assertThat(actual).hasSize(2);
        assertThat(actual).extracting(trainer -> trainer.getUser().getUsername()).containsExactlyInAnyOrder("Callum.Whitfield", "Nora.Pemberton");
    }

    @Test
    void findByUser_UsernameIn_returnsEmpty_whenNoneMatch() {
        List<Trainer> actual = repository.findByUser_UsernameIn(List.of("nobody"));

        assertThat(actual).isEmpty();
    }

    @Test
    void findAllNotAssignedToTrainee_returnsOnlyUnassignedTrainers() {
        List<TrainerInfoDTO> actual = repository.findAllNotAssignedToTrainee("alice");

        assertThat(actual).isNotEmpty().extracting(TrainerInfoDTO::getUsername).doesNotContain("bob");
    }
}