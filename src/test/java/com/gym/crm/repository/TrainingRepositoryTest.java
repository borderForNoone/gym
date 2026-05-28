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
        Training actual = repository.save(buildTraining(getTrainee("alice"), getTrainer("bob"), getTrainingType("Yoga")));

        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getTrainingName()).isEqualTo("Link Test Session");
        assertThat(actual.getTrainingDate()).isEqualTo(LocalDate.of(2024, 10, 10));
        assertThat(actual.getTrainingDuration()).isEqualTo(50);
    }

    @Test
    void save_shouldBeFoundAfterPersist() {
        repository.save(buildTraining(getTrainee("alice"), getTrainer("bob"), getTrainingType("Yoga")));

        Training actual = getLatestTrainingForTrainee("alice");

        assertThat(actual.getTrainingName()).isEqualTo("Link Test Session");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void save_shouldLinkToExistingTraineeAndTrainer() {
        Trainee trainee = getTrainee("alice");
        Trainer trainer = getTrainer("bob");
        TrainingType trainingType = getTrainingType("Yoga");

        Training actual = repository.save(buildTraining(trainee, trainer, trainingType));

        assertThat(countTrainees()).isEqualTo(1L);
        assertThat(countTrainers()).isEqualTo(1L);
        assertThat(actual.getTrainee().getId()).isEqualTo(trainee.getId());
        assertThat(actual.getTrainer().getId()).isEqualTo(trainer.getId());
    }

    @Test
    void findByTraineeCriteria_usernameOnly_returnsBothTrainings() {
        List<Training> actual = repository.findByTraineeCriteria("alice", null, null);

        assertThat(actual).hasSize(2);
        assertThat(actual).extracting(Training::getTrainingName).containsExactlyInAnyOrder("Morning Yoga", "Evening Yoga");
    }

    @Test
    void findByTraineeCriteria_withFromDate_returnsOnlyLaterTraining() {
        List<Training> actual = repository.findByTraineeCriteria("alice", LocalDate.of(2024, 6, 1), null);

        Training training = actual.getFirst();

        assertThat(actual).hasSize(1);
        assertThat(training.getTrainingName()).isEqualTo("Evening Yoga");
        assertThat(training.getTrainingDate()).isEqualTo(LocalDate.of(2024, 9, 20));
    }

    @Test
    void findByTraineeCriteria_withToDate_returnsOnlyEarlierTraining() {
        List<Training> actual = repository.findByTraineeCriteria("alice", null, LocalDate.of(2024, 6, 1));

        Training training = actual.getFirst();

        assertThat(actual).hasSize(1);
        assertThat(training.getTrainingName()).isEqualTo("Morning Yoga");
        assertThat(training.getTrainingDate()).isEqualTo(LocalDate.of(2024, 3, 10));
    }

    @Test
    void findByTraineeCriteria_dateRangeExcludesAllRecords_returnsEmpty() {
        String username = "alice";
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 12, 31);

        List<Training> actual = repository.findByTraineeCriteria(username, from, to);

        assertThat(actual).isEmpty();
    }

    @Test
    void findByTrainerCriteria_usernameOnly_returnsBothTrainings() {
        List<Training> actual = repository.findByTrainerCriteria("bob", null, null);

        assertThat(actual).hasSize(2);
    }

    private <T> T getSingleResult(String jpql, Class<T> type, String param, Object value) {
        return entityManager.createQuery(jpql, type).setParameter(param, value).getSingleResult();
    }

    private Trainee getTrainee(String username) {
        return getSingleResult("""
                SELECT t
                FROM Trainee t
                JOIN t.user u
                WHERE u.username = :username
                """, Trainee.class, "username", username);
    }

    private Trainer getTrainer(String username) {
        return getSingleResult("""
                SELECT t
                FROM Trainer t
                JOIN t.user u
                WHERE u.username = :username
                """, Trainer.class, "username", username);
    }

    private TrainingType getTrainingType(String name) {
        return getSingleResult("""
                SELECT tt
                FROM TrainingType tt
                WHERE tt.trainingTypeName = :name
                """, TrainingType.class, "name", name);
    }

    private Training buildTraining(Trainee trainee, Trainer trainer, TrainingType type) {
        return Training.builder()
                .trainingName("Link Test Session")
                .trainingDate(LocalDate.of(2024, 10, 10))
                .trainingDuration(50)
                .trainee(trainee)
                .trainer(trainer)
                .trainingType(type)
                .build();
    }

    private Long countTrainees() {
        return entityManager.createQuery("""
                        SELECT COUNT(t)
                        FROM Trainee t
                        """, Long.class)
                .getSingleResult();
    }

    private Long countTrainers() {
        return entityManager.createQuery("""
                        SELECT COUNT(t)
                        FROM Trainer t
                        """, Long.class)
                .getSingleResult();
    }

    private Training getLatestTrainingForTrainee(String username) {
        return entityManager.createQuery("""
                        SELECT t
                        FROM Training t
                        JOIN t.trainee tr
                        JOIN tr.user u
                        WHERE u.username = :username
                        ORDER BY t.trainingDate DESC, t.id DESC
                        """, Training.class)
                .setParameter("username", username)
                .setMaxResults(1)
                .getSingleResult();
    }
}