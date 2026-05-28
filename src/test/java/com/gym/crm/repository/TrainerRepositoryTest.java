package com.gym.crm.repository;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.gym.crm.facade.dto.TrainerInfoDTO;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.User;
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
        User updatedUser = existing.getUser().toBuilder().isActive(false).build();
        Trainer updatedTrainer = existing.toBuilder().user(updatedUser).build();

        repository.save(updatedTrainer);

        Trainer actual = repository.findByUser_Username(USERNAME).orElseThrow();
        Boolean actualIsActive = actual.getUser().getIsActive();
        Boolean expectedIsActive = false;

        assertThat(actualIsActive).isEqualTo(expectedIsActive);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_shouldUpdatePassword_whenChangePasswordCalledOnTrainer() {
        Trainer existing = repository.findByUser_Username(USERNAME).orElseThrow();
        User updatedUser = existing.getUser().toBuilder().password("new_encoded_pass").build();
        Trainer updatedTrainer = existing.toBuilder().user(updatedUser).build();

        repository.save(updatedTrainer);

        Trainer actual = repository.findByUser_Username(USERNAME).orElseThrow();
        String actualPassword = actual.getUser().getPassword();
        String expectedPassword = "new_encoded_pass";

        assertThat(actualPassword).isEqualTo(expectedPassword);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_shouldUpdateSpecialization_whenUpdateTrainerCalled() {
        Trainer existing = repository.findByUser_Username(USERNAME).orElseThrow();
        TrainingType pilates = trainingTypeRepository.findByTrainingTypeName("Pilates").orElseThrow();
        Trainer updatedTrainer = existing.toBuilder().specialization(pilates).build();

        repository.save(updatedTrainer);

        Trainer actual = repository.findByUser_Username(USERNAME).orElseThrow();
        String actualSpecialization = actual.getSpecialization().getTrainingTypeName();
        String expectedSpecialization = "Pilates";

        assertThat(actualSpecialization).isEqualTo(expectedSpecialization);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_shouldUpdateName_whenUpdateProfileCalledOnTrainer() {
        Trainer existing = repository.findByUser_Username(USERNAME).orElseThrow();

        User updatedUser = existing.getUser().toBuilder().firstName("CallumUpdated").lastName("WhitfieldUpdated").build();
        Trainer updatedTrainer = existing.toBuilder().user(updatedUser).build();

        repository.save(updatedTrainer);

        Trainer actual = repository.findByUser_Username(USERNAME).orElseThrow();
        String actualFirstName = actual.getUser().getFirstName();
        String expectedFirstName = "CallumUpdated";
        String actualLastName = actual.getUser().getLastName();
        String expectedLastName = "WhitfieldUpdated";

        assertThat(actualFirstName).isEqualTo(expectedFirstName);
        assertThat(actualLastName).isEqualTo(expectedLastName);
    }

    @Test
    void findByUser_Username_returnsTrainer_whenExists() {
        Optional<Trainer> result = repository.findByUser_Username(USERNAME);

        assertThat(result).isPresent();

        Trainer actual = result.get();
        String actualUsername = actual.getUser().getUsername();
        String expectedUsername = "Callum.Whitfield";
        String actualFirstName = actual.getUser().getFirstName();
        String expectedFirstName = "Callum";
        String actualLastName = actual.getUser().getLastName();
        String expectedLastName = "Whitfield";
        String actualPassword = actual.getUser().getPassword();
        String expectedPassword = "pass111";
        Boolean actualIsActive = actual.getUser().getIsActive();
        Boolean expectedIsActive = true;
        String actualSpecialization = actual.getSpecialization().getTrainingTypeName();
        String expectedSpecialization = "Yoga";

        assertThat(actualUsername).isEqualTo(expectedUsername);
        assertThat(actualFirstName).isEqualTo(expectedFirstName);
        assertThat(actualLastName).isEqualTo(expectedLastName);
        assertThat(actualPassword).isEqualTo(expectedPassword);
        assertThat(actualIsActive).isEqualTo(expectedIsActive);
        assertThat(actualSpecialization).isEqualTo(expectedSpecialization);
    }

    @Test
    void findByUser_Username_returnsEmpty_whenNotFound() {
        Optional<Trainer> actual = repository.findByUser_Username("ghost");

        assertThat(actual).isEmpty();
    }

    @Test
    void existsByUser_Username_returnsTrue_whenExists() {
        boolean actual = repository.existsByUser_Username(USERNAME);
        boolean expected = true;

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void existsByUser_Username_returnsFalse_whenNotExists() {
        boolean actual = repository.existsByUser_Username("nobody");
        boolean expected = false;

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void findByUser_UsernameNotIn_excludesGivenUsernames() {
        List<Trainer> actual = repository.findByUser_UsernameNotIn(List.of(USERNAME));
        int actualSize = actual.size();
        int expectedSize = 1;

        assertThat(actualSize).isEqualTo(expectedSize);

        Trainer actualTrainer = actual.getFirst();
        String actualUsername = actualTrainer.getUser().getUsername();
        String expectedUsername = "Nora.Pemberton";
        String actualFirstName = actualTrainer.getUser().getFirstName();
        String expectedFirstName = "Nora";
        String actualLastName = actualTrainer.getUser().getLastName();
        String expectedLastName = "Pemberton";
        String actualPassword = actualTrainer.getUser().getPassword();
        String expectedPassword = "pass222";
        Boolean actualIsActive = actualTrainer.getUser().getIsActive();
        Boolean expectedIsActive = true;
        String actualSpecialization = actualTrainer.getSpecialization().getTrainingTypeName();
        String expectedSpecialization = "Pilates";

        assertThat(actualUsername).isEqualTo(expectedUsername);
        assertThat(actualFirstName).isEqualTo(expectedFirstName);
        assertThat(actualLastName).isEqualTo(expectedLastName);
        assertThat(actualPassword).isEqualTo(expectedPassword);
        assertThat(actualIsActive).isEqualTo(expectedIsActive);
        assertThat(actualSpecialization).isEqualTo(expectedSpecialization);
    }

    @Test
    void findByUser_UsernameNotIn_returnsAll_whenNoUsernameMatches() {
        List<Trainer> actual = repository.findByUser_UsernameNotIn(List.of("nonexistent.user"));
        int actualSize = actual.size();
        int expectedSize = 2;

        assertThat(actualSize).isEqualTo(expectedSize);

        Trainer firstTrainer = actual.get(0);
        Trainer secondTrainer = actual.get(1);
        List<String> actualUsernames = List.of(firstTrainer.getUser().getUsername(), secondTrainer.getUser().getUsername());

        assertThat(actualUsernames).containsExactlyInAnyOrder("Callum.Whitfield", "Nora.Pemberton");
    }

    @Test
    void findByUser_UsernameIn_returnsMatchingTrainers() {
        List<Trainer> actual = repository.findByUser_UsernameIn(List.of(USERNAME, SECOND_USERNAME));
        int actualSize = actual.size();
        int expectedSize = 2;

        assertThat(actualSize).isEqualTo(expectedSize);

        Trainer firstTrainer = actual.get(0);
        Trainer secondTrainer = actual.get(1);
        List<String> actualUsernames = List.of(firstTrainer.getUser().getUsername(), secondTrainer.getUser().getUsername());

        assertThat(actualUsernames).containsExactlyInAnyOrder("Callum.Whitfield", "Nora.Pemberton");
    }

    @Test
    void findByUser_UsernameIn_returnsEmpty_whenNoneMatch() {
        List<Trainer> actual = repository.findByUser_UsernameIn(List.of("nobody"));

        assertThat(actual).isEmpty();
    }

    @Test
    void findByIdNotIn_excludesGivenIds() {
        Trainer callum = repository.findByUser_Username(USERNAME).orElseThrow();
        List<Trainer> actual = repository.findByIdNotIn(List.of(callum.getId()));
        int actualSize = actual.size();
        int expectedSize = 1;

        assertThat(actualSize).isEqualTo(expectedSize);

        Trainer actualTrainer = actual.getFirst();
        Long actualId = actualTrainer.getId();
        Long excludedId = callum.getId();
        String actualUsername = actualTrainer.getUser().getUsername();
        String expectedUsername = "Nora.Pemberton";
        String actualFirstName = actualTrainer.getUser().getFirstName();
        String expectedFirstName = "Nora";
        String actualLastName = actualTrainer.getUser().getLastName();
        String expectedLastName = "Pemberton";
        String actualSpecialization = actualTrainer.getSpecialization().getTrainingTypeName();
        String expectedSpecialization = "Pilates";

        assertThat(actualId).isNotEqualTo(excludedId);
        assertThat(actualUsername).isEqualTo(expectedUsername);
        assertThat(actualFirstName).isEqualTo(expectedFirstName);
        assertThat(actualLastName).isEqualTo(expectedLastName);
        assertThat(actualSpecialization).isEqualTo(expectedSpecialization);
    }

    @Test
    void findByIdNotIn_returnsAll_whenNoIdMatches() {
        List<Trainer> actual = repository.findByIdNotIn(List.of(-1L));
        int actualSize = actual.size();
        int expectedSize = 2;

        assertThat(actualSize).isEqualTo(expectedSize);

        Trainer firstTrainer = actual.get(0);
        Trainer secondTrainer = actual.get(1);
        List<String> actualUsernames = List.of(firstTrainer.getUser().getUsername(), secondTrainer.getUser().getUsername());

        assertThat(actualUsernames).containsExactlyInAnyOrder("Callum.Whitfield", "Nora.Pemberton");
    }

    @Test
    void findAllNotAssignedToTrainee_returnsOnlyUnassignedTrainers() {
        List<TrainerInfoDTO> actual = repository.findAllNotAssignedToTrainee("alice");

        assertThat(actual).isNotEmpty();

        List<String> actualUsernames = actual.stream()
                .map(TrainerInfoDTO::getUsername)
                .toList();

        assertThat(actualUsernames).doesNotContain("bob");
    }
}