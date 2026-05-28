package com.gym.crm.repository;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DatabaseSetup("/dataset/training-dataset.xml")
@DatabaseTearDown(value = "/dataset/training-dataset.xml", type = DatabaseOperation.DELETE_ALL)
class TrainingRepositoryTest extends BaseTestRepository<TrainingRepository> {
    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_shouldPersistTraining() {
        Trainee trainee = (Trainee) entityManager.createQuery("SELECT t FROM Trainee t JOIN t.user u WHERE u.username = 'alice'").getSingleResult();
        Trainer trainer = (Trainer) entityManager.createQuery("SELECT t FROM Trainer t JOIN t.user u WHERE u.username = 'bob'").getSingleResult();
        TrainingType trainingType = (TrainingType) entityManager.createQuery("SELECT tt FROM TrainingType tt WHERE tt.trainingTypeName = 'Yoga'").getSingleResult();
        Training training = Training.builder()
                .trainingName("New Cardio Session")
                .trainingDate(LocalDate.of(2024, 11, 1))
                .trainingDuration(45)
                .trainee(trainee)
                .trainer(trainer)
                .trainingType(trainingType)
                .build();

        Training actual = repository.save(training);

        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getTrainingName()).isEqualTo("New Cardio Session");
        assertThat(actual.getTrainingDate()).isEqualTo(LocalDate.of(2024, 11, 1));
        assertThat(actual.getTrainingDuration()).isEqualTo(45);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_shouldBeFoundAfterPersist() {
        Trainee trainee = (Trainee) entityManager.createQuery("""
                        SELECT t FROM Trainee t JOIN FETCH t.user WHERE t.user.username = 'alice'
                """).getSingleResult();
        Trainer trainer = (Trainer) entityManager.createQuery("""
                        SELECT t FROM Trainer t JOIN FETCH t.user WHERE t.user.username = 'bob'
                """).getSingleResult();
        TrainingType trainingType = (TrainingType) entityManager.createQuery("""
                        SELECT tt FROM TrainingType tt WHERE tt.trainingTypeName = 'Yoga'
                """).getSingleResult();
        Training training = Training.builder()
                .trainingName("Round Trip Session")
                .trainingDate(LocalDate.of(2024, 12, 5))
                .trainingDuration(30)
                .trainee(trainee)
                .trainer(trainer)
                .trainingType(trainingType)
                .build();

        Training saved = repository.save(training);
        Training actual = (Training) entityManager.createQuery("""
                        SELECT t FROM Training t
                        JOIN FETCH t.trainee tr
                        JOIN FETCH tr.user
                        JOIN FETCH t.trainer tn
                        JOIN FETCH tn.user
                        JOIN FETCH t.trainingType
                        WHERE t.id = :id
                """).setParameter("id", saved.getId()).getSingleResult();

        assertThat(actual.getTrainingName()).isEqualTo("Round Trip Session");
        assertThat(actual.getTrainingDate()).isEqualTo(LocalDate.of(2024, 12, 5));
        assertThat(actual.getTrainingDuration()).isEqualTo(30);
        assertThat(actual.getTrainee().getUser().getUsername()).isEqualTo("alice");
        assertThat(actual.getTrainer().getUser().getUsername()).isEqualTo("bob");
        assertThat(actual.getTrainingType().getTrainingTypeName()).isEqualTo("Yoga");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_shouldLinkToExistingTraineeAndTrainer() {
        Trainee trainee = (Trainee) entityManager.createQuery("""
                SELECT t
                FROM Trainee t
                JOIN t.user u
                WHERE u.username = 'alice'
                """).getSingleResult();
        Trainer trainer = (Trainer) entityManager.createQuery("""
                SELECT t
                FROM Trainer t
                JOIN t.user u
                WHERE u.username = 'bob'
                """).getSingleResult();
        TrainingType trainingType = (TrainingType) entityManager.createQuery("""
                SELECT tt
                FROM TrainingType tt
                WHERE tt.trainingTypeName = 'Yoga'
                """).getSingleResult();

        Training actual = repository.save(
                Training.builder()
                        .trainingName("Link Test Session")
                        .trainingDate(LocalDate.of(2024, 10, 10))
                        .trainingDuration(50)
                        .trainee(trainee)
                        .trainer(trainer)
                        .trainingType(trainingType)
                        .build()
        );

        Long actualTraineeCount = (Long) entityManager.createQuery("""
                SELECT COUNT(t)
                FROM Trainee t
                """).getSingleResult();
        Long actualTrainerCount = (Long) entityManager.createQuery("""
                SELECT COUNT(t)
                FROM Trainer t
                """).getSingleResult();
        Long actualTraineeId = actual.getTrainee().getId();
        Long actualTrainerId = actual.getTrainer().getId();
        Long expectedTraineeCount = 1L;
        Long expectedTrainerCount = 1L;
        Long expectedTraineeId = trainee.getId();
        Long expectedTrainerId = trainer.getId();

        assertThat(actualTraineeCount).isEqualTo(expectedTraineeCount);
        assertThat(actualTrainerCount).isEqualTo(expectedTrainerCount);
        assertThat(actualTraineeId).isEqualTo(expectedTraineeId);
        assertThat(actualTrainerId).isEqualTo(expectedTrainerId);
    }

    @Test
    void findByTraineeCriteria_usernameOnly_returnsBothTrainings() {
        List<Training> actual = repository.findByTraineeCriteria("alice", null, null);

        int actualSize = actual.size();
        int expectedSize = 2;
        Training firstTraining = actual.get(0);
        Training secondTraining = actual.get(1);
        List<String> actualTrainingNames = List.of(firstTraining.getTrainingName(), secondTraining.getTrainingName());
        List<String> expectedTrainingNames = List.of("Morning Yoga", "Evening Yoga");

        assertThat(actualSize).isEqualTo(expectedSize);
        assertThat(actualTrainingNames).containsExactlyInAnyOrderElementsOf(expectedTrainingNames);
    }


    @Test
    void findByTraineeCriteria_withFromDate_returnsOnlyLaterTraining() {
        LocalDate from = LocalDate.of(2024, 6, 1);
        List<Training> actual = repository.findByTraineeCriteria("alice", from, null);

        int actualSize = actual.size();
        int expectedSize = 1;
        Training actualTraining = actual.getFirst();
        String actualTrainingName = actualTraining.getTrainingName();
        LocalDate actualTrainingDate = actualTraining.getTrainingDate();
        String expectedTrainingName = "Evening Yoga";
        LocalDate expectedTrainingDate = LocalDate.of(2024, 9, 20);

        assertThat(actualSize).isEqualTo(expectedSize);
        assertThat(actualTrainingName).isEqualTo(expectedTrainingName);
        assertThat(actualTrainingDate).isEqualTo(expectedTrainingDate);
    }

    @Test
    void findByTraineeCriteria_withToDate_returnsOnlyEarlierTraining() {
        LocalDate to = LocalDate.of(2024, 6, 1);
        List<Training> actual = repository.findByTraineeCriteria("alice", null, to);

        int actualSize = actual.size();
        int expectedSize = 1;
        Training actualTraining = actual.getFirst();
        String actualTrainingName = actualTraining.getTrainingName();
        LocalDate actualTrainingDate = actualTraining.getTrainingDate();
        String expectedTrainingName = "Morning Yoga";
        LocalDate expectedTrainingDate = LocalDate.of(2024, 3, 10);

        assertThat(actualSize).isEqualTo(expectedSize);
        assertThat(actualTrainingName).isEqualTo(expectedTrainingName);
        assertThat(actualTrainingDate).isEqualTo(expectedTrainingDate);
    }

    @Test
    void findByTraineeCriteria_withExactDateRange_returnsSingleMatch() {
        LocalDate from = LocalDate.of(2024, 3, 1);
        LocalDate to = LocalDate.of(2024, 4, 1);
        List<Training> actual = repository.findByTraineeCriteria("alice", from, to);

        int actualSize = actual.size();
        int expectedSize = 1;
        Training actualTraining = actual.getFirst();
        String actualTrainingName = actualTraining.getTrainingName();
        LocalDate actualTrainingDate = actualTraining.getTrainingDate();
        String expectedTrainingName = "Morning Yoga";
        LocalDate expectedTrainingDate = LocalDate.of(2024, 3, 10);

        assertThat(actualSize).isEqualTo(expectedSize);
        assertThat(actualTrainingName).isEqualTo(expectedTrainingName);
        assertThat(actualTrainingDate).isEqualTo(expectedTrainingDate);
    }

    @Test
    void findByTraineeCriteria_nullUsername_returnsAllTrainings() {
        List<Training> actual = repository.findByTraineeCriteria(null, null, null);

        int actualSize = actual.size();
        int expectedSize = 2;

        assertThat(actualSize).isEqualTo(expectedSize);
    }

    @Test
    void findByTraineeCriteria_unknownUsername_returnsEmpty() {
        List<Training> actual = repository.findByTraineeCriteria("ghost", null, null);

        assertThat(actual).isEmpty();
    }

    @Test
    void findByTraineeCriteria_dateRangeExcludesAllRecords_returnsEmpty() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 12, 31);

        List<Training> actual = repository.findByTraineeCriteria("alice", from, to);

        assertThat(actual).isEmpty();
    }

    @Test
    void findByTrainerCriteria_usernameOnly_returnsBothTrainings() {
        List<Training> actual = repository.findByTrainerCriteria("bob", null, null);

        int actualSize = actual.size();
        int expectedSize = 2;

        assertThat(actualSize).isEqualTo(expectedSize);
    }

    @Test
    void findByTrainerCriteria_withFromDate_returnsOnlyLaterTraining() {
        LocalDate from = LocalDate.of(2024, 6, 1);
        List<Training> actual = repository.findByTrainerCriteria("bob", from, null);

        int actualSize = actual.size();
        int expectedSize = 1;
        Training actualTraining = actual.getFirst();
        String actualTrainingName = actualTraining.getTrainingName();
        String expectedTrainingName = "Evening Yoga";

        assertThat(actualSize).isEqualTo(expectedSize);
        assertThat(actualTrainingName).isEqualTo(expectedTrainingName);
    }

    @Test
    void findByTrainerCriteria_withToDate_returnsOnlyEarlierTraining() {
        LocalDate to = LocalDate.of(2024, 6, 1);
        List<Training> actual = repository.findByTrainerCriteria("bob", null, to);

        int actualSize = actual.size();
        int expectedSize = 1;
        Training actualTraining = actual.getFirst();
        String actualTrainingName = actualTraining.getTrainingName();
        String expectedTrainingName = "Morning Yoga";

        assertThat(actualSize).isEqualTo(expectedSize);
        assertThat(actualTrainingName).isEqualTo(expectedTrainingName);
    }

    @Test
    void findByTrainerCriteria_unknownUsername_returnsEmpty() {
        List<Training> actual = repository.findByTrainerCriteria("nobody", null, null);

        assertThat(actual).isEmpty();
    }

    @Test
    void findTraineeTrainings_usernameOnly_returnsAllForTrainee() {
        List<Training> actual = repository.findTraineeTrainings("alice", null, null);

        int actualSize = actual.size();
        int expectedSize = 2;

        assertThat(actualSize).isEqualTo(expectedSize);
    }

    @Test
    void findTraineeTrainings_withDateRange_returnsSingleMatch() {
        LocalDate from = LocalDate.of(2024, 9, 1);
        LocalDate to = LocalDate.of(2024, 9, 30);
        List<Training> actual = repository.findTraineeTrainings("alice", from, to);

        int actualSize = actual.size();
        int expectedSize = 1;
        Training actualTraining = actual.getFirst();
        String actualTrainingName = actualTraining.getTrainingName();
        LocalDate actualTrainingDate = actualTraining.getTrainingDate();
        String expectedTrainingName = "Evening Yoga";
        LocalDate expectedTrainingDate = LocalDate.of(2024, 9, 20);

        assertThat(actualSize).isEqualTo(expectedSize);
        assertThat(actualTrainingName).isEqualTo(expectedTrainingName);
        assertThat(actualTrainingDate).isEqualTo(expectedTrainingDate);
    }

    @Test
    void findTraineeTrainings_unknownUsername_returnsEmpty() {
        List<Training> actual = repository.findTraineeTrainings("ghost", null, null);

        assertThat(actual).isEmpty();
    }
}