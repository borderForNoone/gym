package com.gym.crm.repository;

import com.gym.crm.facade.dto.TrainerInfoDTO;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TrainerRepositoryTest extends BaseRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private TrainerRepository trainerRepository;

    private Trainer trainerAlpha;
    private Trainer trainerBeta;
    private Trainee traineeAlice;
    private TrainingType type;

    @BeforeEach
    void setUp() {
        type = entityManager.persistAndFlush(TrainingType.builder().trainingTypeName("Yoga").build());
        trainerAlpha = entityManager.persistFlushFind(buildTrainer("alpha.trainer", "Alpha", "Trainer"));
        trainerBeta = entityManager.persistFlushFind(buildTrainer("beta.trainer", "Beta", "Trainer"));
        traineeAlice = entityManager.persistFlushFind(Trainee.builder().user(buildUser("alice.trainee", "Alice", "Smith"))
                .dateOfBirth(LocalDate.of(1995, 1, 1)).trainers(new java.util.HashSet<>(java.util.Set.of(trainerAlpha))).build());
        entityManager.flush();
    }

    @Test
    void findByUser_Username_returnsTrainer_whenExists() {
        Optional<Trainer> result = trainerRepository.findByUser_Username("alpha.trainer");

        assertThat(result).isPresent();
        assertThat(result.get().getUser().getFirstName()).isEqualTo("Alpha");
    }

    @Test
    void findByUser_Username_returnsEmpty_whenNotFound() {
        assertThat(trainerRepository.findByUser_Username("ghost")).isEmpty();
    }

    @Test
    void existsByUser_Username_returnsTrue_whenExists() {
        assertThat(trainerRepository.existsByUser_Username("alpha.trainer")).isTrue();
    }

    @Test
    void existsByUser_Username_returnsFalse_whenNotExists() {
        assertThat(trainerRepository.existsByUser_Username("nobody")).isFalse();
    }

    @Test
    void findByUser_UsernameNotIn_excludesGivenUsernames() {
        List<Trainer> result = trainerRepository.findByUser_UsernameNotIn(List.of("alpha.trainer"));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUser().getUsername()).isEqualTo("beta.trainer");
    }

    @Test
    void findByUser_UsernameNotIn_returnsAll_whenNoUsernameMatches() {
        List<Trainer> result = trainerRepository.findByUser_UsernameNotIn(List.of("nonexistent.user"));

        assertThat(result).extracting(t -> t.getUser().getUsername()).containsExactlyInAnyOrder("alpha.trainer", "beta.trainer");
    }

    @Test
    void findByUser_UsernameIn_returnsMatchingTrainers() {
        List<Trainer> result = trainerRepository.findByUser_UsernameIn(List.of("alpha.trainer", "beta.trainer"));

        assertThat(result).extracting(t -> t.getUser().getUsername()).containsExactlyInAnyOrder("alpha.trainer", "beta.trainer");
    }

    @Test
    void findByUser_UsernameIn_returnsEmpty_whenNoneMatch() {
        assertThat(trainerRepository.findByUser_UsernameIn(List.of("nobody"))).isEmpty();
    }

    @Test
    void findByIdNotIn_excludesGivenIds() {
        List<Trainer> result = trainerRepository.findByIdNotIn(List.of(trainerAlpha.getId()));

        assertThat(result).extracting(Trainer::getId).doesNotContain(trainerAlpha.getId());
    }

    @Test
    void findByIdNotIn_returnsAll_whenNoIdMatches() {
        assertThat(trainerRepository.findByIdNotIn(List.of(-1L))).extracting(t -> t.getUser().getUsername())
                .containsExactlyInAnyOrder("alpha.trainer", "beta.trainer");
    }

    @Test
    void findAllNotAssignedToTrainee_returnsBetaOnly_forAliceWhoHasAlpha() {
        List<TrainerInfoDTO> result = trainerRepository.findAllNotAssignedToTrainee("alice.trainee");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUsername()).isEqualTo("beta.trainer");
    }

    @Test
    void findAllNotAssignedToTrainee_returnsAllTrainers_forTraineeWithNoAssignments() {
        entityManager.persistFlushFind(Trainee.builder().user(buildUser("bob.trainee", "Bob", "Jones"))
                .dateOfBirth(LocalDate.of(1990, 1, 1)).build());

        List<TrainerInfoDTO> result = trainerRepository.findAllNotAssignedToTrainee("bob.trainee");

        assertThat(result).extracting(TrainerInfoDTO::getUsername).containsExactlyInAnyOrder("alpha.trainer", "beta.trainer");
    }

    @Test
    void findAllNotAssignedToTrainee_returnsEmpty_whenAllTrainersAssigned() {
        Trainee withBothTrainers = traineeAlice.toBuilder().trainers(new HashSet<>(Set.of(trainerAlpha, trainerBeta))).build();
        entityManager.merge(withBothTrainers);
        entityManager.flush();
        entityManager.clear();

        List<TrainerInfoDTO> result = trainerRepository.findAllNotAssignedToTrainee("alice.trainee");

        assertThat(result).isEmpty();
    }

    private User buildUser(String username, String firstName, String lastName) {
        return User.builder().username(username).firstName(firstName).lastName(lastName).password("pass").isActive(true).build();
    }

    private Trainer buildTrainer(String username, String firstName, String lastName) {
        return Trainer.builder().user(buildUser(username, firstName, lastName)).specialization(type).build();
    }
}