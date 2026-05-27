package com.gym.crm.repository;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.gym.crm.model.Training;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DatabaseSetup("/dataset/training-dataset.xml")
class TrainingRepositoryTest extends BaseTestRepository<TrainingRepository> {
    @Test
    void findByTraineeCriteria_usernameOnly_returnsBothTrainings() {
        List<Training> result = repository.findByTraineeCriteria("alice", null, null);

        assertThat(result).hasSize(2);

        assertThat(result).anySatisfy(t -> {
            assertThat(t.getTrainingName()).isEqualTo("Morning Yoga");
            assertThat(t.getTrainingDate()).isEqualTo(LocalDate.of(2024, 3, 10));
            assertThat(t.getTrainingDuration()).isEqualTo(60);
            assertThat(t.getTrainee().getUser().getUsername()).isEqualTo("alice");
            assertThat(t.getTrainer().getUser().getUsername()).isEqualTo("bob");
            assertThat(t.getTrainingType().getTrainingTypeName()).isEqualTo("Yoga");
        });

        assertThat(result).anySatisfy(t -> {
            assertThat(t.getTrainingName()).isEqualTo("Evening Yoga");
            assertThat(t.getTrainingDate()).isEqualTo(LocalDate.of(2024, 9, 20));
            assertThat(t.getTrainingDuration()).isEqualTo(60);
            assertThat(t.getTrainee().getUser().getUsername()).isEqualTo("alice");
            assertThat(t.getTrainer().getUser().getUsername()).isEqualTo("bob");
            assertThat(t.getTrainingType().getTrainingTypeName()).isEqualTo("Yoga");
        });
    }

    @Test
    void findByTraineeCriteria_withFromDate_returnsOnlyLaterTraining() {
        LocalDate from = LocalDate.of(2024, 6, 1);

        List<Training> result = repository.findByTraineeCriteria("alice", from, null);

        assertThat(result).hasSize(1);

        Training training = result.getFirst();
        assertThat(training.getTrainingName()).isEqualTo("Evening Yoga");
        assertThat(training.getTrainingDate()).isEqualTo(LocalDate.of(2024, 9, 20));
        assertThat(training.getTrainingDuration()).isEqualTo(60);
        assertThat(training.getTrainee().getUser().getUsername()).isEqualTo("alice");
        assertThat(training.getTrainer().getUser().getUsername()).isEqualTo("bob");
        assertThat(training.getTrainingType().getTrainingTypeName()).isEqualTo("Yoga");
    }

    @Test
    void findByTraineeCriteria_withToDate_returnsOnlyEarlierTraining() {
        LocalDate to = LocalDate.of(2024, 6, 1);

        List<Training> result = repository.findByTraineeCriteria("alice", null, to);

        assertThat(result).hasSize(1);

        Training training = result.getFirst();
        assertThat(training.getTrainingName()).isEqualTo("Morning Yoga");
        assertThat(training.getTrainingDate()).isEqualTo(LocalDate.of(2024, 3, 10));
        assertThat(training.getTrainingDuration()).isEqualTo(60);
        assertThat(training.getTrainee().getUser().getUsername()).isEqualTo("alice");
        assertThat(training.getTrainer().getUser().getUsername()).isEqualTo("bob");
        assertThat(training.getTrainingType().getTrainingTypeName()).isEqualTo("Yoga");
    }

    @Test
    void findByTraineeCriteria_withExactDateRange_returnsSingleMatch() {
        LocalDate from = LocalDate.of(2024, 3, 1);
        LocalDate to = LocalDate.of(2024, 4, 1);

        List<Training> result = repository.findByTraineeCriteria("alice", from, to);

        assertThat(result).hasSize(1);

        Training training = result.getFirst();
        assertThat(training.getTrainingName()).isEqualTo("Morning Yoga");
        assertThat(training.getTrainingDate()).isEqualTo(LocalDate.of(2024, 3, 10));
        assertThat(training.getTrainingDuration()).isEqualTo(60);
        assertThat(training.getTrainee().getUser().getUsername()).isEqualTo("alice");
        assertThat(training.getTrainer().getUser().getUsername()).isEqualTo("bob");
        assertThat(training.getTrainingType().getTrainingTypeName()).isEqualTo("Yoga");
    }

    @Test
    void findByTraineeCriteria_nullUsername_returnsAllTrainings() {
        List<Training> result = repository.findByTraineeCriteria(null, null, null);

        assertThat(result).hasSize(2);

        assertThat(result).anySatisfy(t -> {
            assertThat(t.getTrainingName()).isEqualTo("Morning Yoga");
            assertThat(t.getTrainingDate()).isEqualTo(LocalDate.of(2024, 3, 10));
        });

        assertThat(result).anySatisfy(t -> {
            assertThat(t.getTrainingName()).isEqualTo("Evening Yoga");
            assertThat(t.getTrainingDate()).isEqualTo(LocalDate.of(2024, 9, 20));
        });
    }

    @Test
    void findByTraineeCriteria_unknownUsername_returnsEmpty() {
        List<Training> result = repository.findByTraineeCriteria("ghost", null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void findByTraineeCriteria_dateRangeExcludesAllRecords_returnsEmpty() {
        List<Training> result = repository.findByTraineeCriteria(
                "alice", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31)
        );

        assertThat(result).isEmpty();
    }

    @Test
    void findByTrainerCriteria_usernameOnly_returnsBothTrainings() {
        List<Training> result = repository.findByTrainerCriteria("bob", null, null);

        assertThat(result).hasSize(2);

        assertThat(result).anySatisfy(t -> {
            assertThat(t.getTrainingName()).isEqualTo("Morning Yoga");
            assertThat(t.getTrainingDate()).isEqualTo(LocalDate.of(2024, 3, 10));
            assertThat(t.getTrainingDuration()).isEqualTo(60);
            assertThat(t.getTrainer().getUser().getUsername()).isEqualTo("bob");
            assertThat(t.getTrainingType().getTrainingTypeName()).isEqualTo("Yoga");
        });

        assertThat(result).anySatisfy(t -> {
            assertThat(t.getTrainingName()).isEqualTo("Evening Yoga");
            assertThat(t.getTrainingDate()).isEqualTo(LocalDate.of(2024, 9, 20));
            assertThat(t.getTrainingDuration()).isEqualTo(60);
            assertThat(t.getTrainer().getUser().getUsername()).isEqualTo("bob");
            assertThat(t.getTrainingType().getTrainingTypeName()).isEqualTo("Yoga");
        });
    }

    @Test
    void findByTrainerCriteria_withFromDate_returnsOnlyLaterTraining() {
        LocalDate from = LocalDate.of(2024, 6, 1);

        List<Training> result = repository.findByTrainerCriteria("bob", from, null);

        assertThat(result).hasSize(1);

        Training training = result.getFirst();
        assertThat(training.getTrainingName()).isEqualTo("Evening Yoga");
        assertThat(training.getTrainingDate()).isEqualTo(LocalDate.of(2024, 9, 20));
        assertThat(training.getTrainingDuration()).isEqualTo(60);
        assertThat(training.getTrainer().getUser().getUsername()).isEqualTo("bob");
        assertThat(training.getTrainingType().getTrainingTypeName()).isEqualTo("Yoga");
    }

    @Test
    void findByTrainerCriteria_withToDate_returnsOnlyEarlierTraining() {
        LocalDate to = LocalDate.of(2024, 6, 1);

        List<Training> result = repository.findByTrainerCriteria("bob", null, to);

        assertThat(result).hasSize(1);

        Training training = result.getFirst();
        assertThat(training.getTrainingName()).isEqualTo("Morning Yoga");
        assertThat(training.getTrainingDate()).isEqualTo(LocalDate.of(2024, 3, 10));
        assertThat(training.getTrainingDuration()).isEqualTo(60);
        assertThat(training.getTrainer().getUser().getUsername()).isEqualTo("bob");
        assertThat(training.getTrainingType().getTrainingTypeName()).isEqualTo("Yoga");
    }

    @Test
    void findByTrainerCriteria_unknownUsername_returnsEmpty() {
        List<Training> result = repository.findByTrainerCriteria("nobody", null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void findTraineeTrainings_usernameOnly_returnsAllForTrainee() {
        List<Training> result = repository.findTraineeTrainings("alice", null, null);

        assertThat(result).hasSize(2);

        assertThat(result).anySatisfy(t -> {
            assertThat(t.getTrainingName()).isEqualTo("Morning Yoga");
            assertThat(t.getTrainingDate()).isEqualTo(LocalDate.of(2024, 3, 10));
            assertThat(t.getTrainingDuration()).isEqualTo(60);
            assertThat(t.getTrainee().getUser().getUsername()).isEqualTo("alice");
            assertThat(t.getTrainingType().getTrainingTypeName()).isEqualTo("Yoga");
        });

        assertThat(result).anySatisfy(t -> {
            assertThat(t.getTrainingName()).isEqualTo("Evening Yoga");
            assertThat(t.getTrainingDate()).isEqualTo(LocalDate.of(2024, 9, 20));
            assertThat(t.getTrainingDuration()).isEqualTo(60);
            assertThat(t.getTrainee().getUser().getUsername()).isEqualTo("alice");
            assertThat(t.getTrainingType().getTrainingTypeName()).isEqualTo("Yoga");
        });
    }

    @Test
    void findTraineeTrainings_withDateRange_returnsSingleMatch() {
        LocalDate from = LocalDate.of(2024, 9, 1);
        LocalDate to = LocalDate.of(2024, 9, 30);

        List<Training> result = repository.findTraineeTrainings("alice", from, to);

        assertThat(result).hasSize(1);

        Training training = result.getFirst();
        assertThat(training.getTrainingName()).isEqualTo("Evening Yoga");
        assertThat(training.getTrainingDate()).isEqualTo(LocalDate.of(2024, 9, 20));
        assertThat(training.getTrainingDuration()).isEqualTo(60);
        assertThat(training.getTrainee().getUser().getUsername()).isEqualTo("alice");
        assertThat(training.getTrainingType().getTrainingTypeName()).isEqualTo("Yoga");
    }

    @Test
    void findTraineeTrainings_unknownUsername_returnsEmpty() {
        List<Training> result = repository.findTraineeTrainings("ghost", null, null);

        assertThat(result).isEmpty();
    }
}