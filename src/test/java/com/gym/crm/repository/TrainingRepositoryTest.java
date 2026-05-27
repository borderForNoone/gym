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
        assertThat(repository.findByTraineeCriteria("alice", null, null)).hasSize(2);
    }

    @Test
    void findByTraineeCriteria_withFromDate_returnsOnlyLaterTraining() {
        List<Training> result = repository.findByTraineeCriteria("alice", LocalDate.of(2024, 6, 1), null);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTrainingName()).isEqualTo("Evening Yoga");
    }

    @Test
    void findByTraineeCriteria_withToDate_returnsOnlyEarlierTraining() {
        List<Training> result = repository.findByTraineeCriteria("alice", null, LocalDate.of(2024, 6, 1));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTrainingName()).isEqualTo("Morning Yoga");
    }

    @Test
    void findByTraineeCriteria_withExactDateRange_returnsSingleMatch() {
        List<Training> result = repository.findByTraineeCriteria("alice", LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 4, 1));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTrainingName()).isEqualTo("Morning Yoga");
    }

    @Test
    void findByTraineeCriteria_nullUsername_returnsBothTrainings() {
        assertThat(repository.findByTraineeCriteria(null, null, null)).hasSize(2);
    }

    @Test
    void findByTraineeCriteria_unknownUsername_returnsEmpty() {
        assertThat(repository.findByTraineeCriteria("ghost", null, null)).isEmpty();
    }

    @Test
    void findByTraineeCriteria_dateRangeExcludesAllRecords_returnsEmpty() {
        assertThat(repository.findByTraineeCriteria("alice", LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31))).isEmpty();
    }

    @Test
    void findByTrainerCriteria_usernameOnly_returnsBothTrainings() {
        assertThat(repository.findByTrainerCriteria("bob", null, null)).hasSize(2);
    }

    @Test
    void findByTrainerCriteria_withFromDate_returnsOnlyLaterTraining() {
        List<Training> result = repository.findByTrainerCriteria("bob", LocalDate.of(2024, 6, 1), null);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTrainingName()).isEqualTo("Evening Yoga");
    }

    @Test
    void findByTrainerCriteria_withToDate_returnsOnlyEarlierTraining() {
        List<Training> result = repository.findByTrainerCriteria("bob", null, LocalDate.of(2024, 6, 1));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTrainingName()).isEqualTo("Morning Yoga");
    }

    @Test
    void findByTrainerCriteria_unknownUsername_returnsEmpty() {
        assertThat(repository.findByTrainerCriteria("nobody", null, null)).isEmpty();
    }

    @Test
    void findTraineeTrainings_usernameOnly_returnsAllForTrainee() {
        assertThat(repository.findTraineeTrainings("alice", null, null)).hasSize(2);
    }

    @Test
    void findTraineeTrainings_withDateRange_returnsSingleMatch() {
        List<Training> result = repository.findTraineeTrainings("alice", LocalDate.of(2024, 9, 1),
                LocalDate.of(2024, 9, 30));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTrainingName()).isEqualTo("Evening Yoga");
    }

    @Test
    void findTraineeTrainings_unknownUsername_returnsEmpty() {
        assertThat(repository.findTraineeTrainings("ghost", null, null)).isEmpty();
    }
}