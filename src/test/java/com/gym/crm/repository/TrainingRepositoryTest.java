package com.gym.crm.repository;

import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TrainingRepositoryTest extends BaseRepositoryTest {
    private static final LocalDate MARCH = LocalDate.of(2024, 3, 10);
    private static final LocalDate SEPTEMBER = LocalDate.of(2024, 9, 20);

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private TrainingRepository trainingRepository;

    private Trainee traineeAlice;
    private Trainer trainerBob;
    private TrainingType type;

    @BeforeEach
    void setUp() {
        type = entityManager.persistAndFlush(TrainingType.builder().trainingTypeName("Yoga").build());
        traineeAlice = entityManager.persistFlushFind(Trainee.builder().user(buildUser("alice", "Alice", "Smith"))
                .dateOfBirth(LocalDate.of(1995, 1, 1)).build());
        trainerBob = entityManager.persistFlushFind(Trainer.builder().user(buildUser("bob", "Bob", "Jones")).specialization(type).build());
        entityManager.persistAndFlush(buildTraining("Morning Yoga", MARCH));
        entityManager.persistAndFlush(buildTraining("Evening Yoga", SEPTEMBER));
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByTraineeCriteria_usernameOnly_returnsBothTrainings() {
        assertThat(trainingRepository.findByTraineeCriteria("alice", null, null)).hasSize(2);
    }

    @Test
    void findByTraineeCriteria_withFromDate_returnsOnlyLaterTraining() {
        List<Training> result = trainingRepository.findByTraineeCriteria("alice", LocalDate.of(2024, 6, 1), null);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTrainingName()).isEqualTo("Evening Yoga");
    }

    @Test
    void findByTraineeCriteria_withToDate_returnsOnlyEarlierTraining() {
        List<Training> result = trainingRepository.findByTraineeCriteria("alice", null, LocalDate.of(2024, 6, 1));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTrainingName()).isEqualTo("Morning Yoga");
    }

    @Test
    void findByTraineeCriteria_withExactDateRange_returnsSingleMatch() {
        List<Training> result = trainingRepository.findByTraineeCriteria("alice", LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 4, 1));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTrainingName()).isEqualTo("Morning Yoga");
    }

    @Test
    void findByTraineeCriteria_nullUsername_returnsAllRecords() {
        assertThat(trainingRepository.findByTraineeCriteria(null, null, null)).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void findByTraineeCriteria_unknownUsername_returnsEmpty() {
        assertThat(trainingRepository.findByTraineeCriteria("ghost", null, null)).isEmpty();
    }

    @Test
    void findByTraineeCriteria_dateRangeExcludesAllRecords_returnsEmpty() {
        assertThat(trainingRepository.findByTraineeCriteria("alice", LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31))).isEmpty();
    }

    @Test
    void findByTrainerCriteria_usernameOnly_returnsBothTrainings() {
        assertThat(trainingRepository.findByTrainerCriteria("bob", null, null)).hasSize(2);
    }

    @Test
    void findByTrainerCriteria_withFromDate_returnsOnlyLaterTraining() {
        List<Training> result = trainingRepository.findByTrainerCriteria("bob", LocalDate.of(2024, 6, 1), null);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTrainingName()).isEqualTo("Evening Yoga");
    }

    @Test
    void findByTrainerCriteria_withToDate_returnsOnlyEarlierTraining() {
        List<Training> result = trainingRepository.findByTrainerCriteria("bob", null, LocalDate.of(2024, 6, 1));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTrainingName()).isEqualTo("Morning Yoga");
    }

    @Test
    void findByTrainerCriteria_unknownUsername_returnsEmpty() {
        assertThat(trainingRepository.findByTrainerCriteria("nobody", null, null)).isEmpty();
    }

    @Test
    void findTraineeTrainings_usernameOnly_returnsAllForTrainee() {
        assertThat(trainingRepository.findTraineeTrainings("alice", null, null)).hasSize(2);
    }

    @Test
    void findTraineeTrainings_withDateRange_returnsSingleMatch() {
        List<Training> result = trainingRepository.findTraineeTrainings("alice", LocalDate.of(2024, 9, 1),
                LocalDate.of(2024, 9, 30));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTrainingName()).isEqualTo("Evening Yoga");
    }

    @Test
    void findTraineeTrainings_unknownUsername_returnsEmpty() {
        assertThat(trainingRepository.findTraineeTrainings("ghost", null, null)).isEmpty();
    }

    private User buildUser(String username, String firstName, String lastName) {
        return User.builder().username(username).firstName(firstName).lastName(lastName).password("pass").isActive(true).build();
    }

    private Training buildTraining(String name, LocalDate date) {
        return Training.builder().trainingName(name).trainee(traineeAlice).trainer(trainerBob).trainingType(type).trainingDate(date).trainingDuration(60).build();
    }
}