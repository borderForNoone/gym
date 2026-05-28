package com.gym.crm.repository;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.gym.crm.exception.ValidationFailedException;
import com.gym.crm.model.Training;
import com.gym.crm.repository.impl.TrainingRepositoryCriteriaImpl;
import com.gym.crm.search.criteria.TraineeTrainingCriteriaBuilder;
import com.gym.crm.search.criteria.TrainerTrainingCriteriaBuilder;
import com.gym.crm.search.filter.TraineeTrainingFilter;
import com.gym.crm.search.filter.TrainerTrainingFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Import({TrainingRepositoryCriteriaImpl.class, TraineeTrainingCriteriaBuilder.class, TrainerTrainingCriteriaBuilder.class})
@DatabaseSetup("/dataset/training-dataset.xml")
class TrainingRepositoryCriteriaTest extends BaseTestRepository<TrainingRepositoryCriteria> {
    private static final String TRAINER_USERNAME = "bob";
    private static final String TRAINEE_USERNAME1 = "alice";
    private static final String YOGA = "Yoga";

    @Test
    void findByTraineeCriteria_shouldThrowException_whenNullFilter() {
        ValidationFailedException exception = assertThrows(ValidationFailedException.class,
                () -> repository.findByTraineeCriteria(null));

        assertThat(exception.getMessage()).isEqualTo("Filter cannot be null");
    }

    @Test
    void findByTraineeCriteria_shouldThrowException_whenNoUsername() {
        TraineeTrainingFilter filter = TraineeTrainingFilter.builder().build();
        ValidationFailedException exception = assertThrows(ValidationFailedException.class,
                () -> repository.findByTraineeCriteria(filter));

        assertThat(exception.getMessage()).isEqualTo("Username cannot be null or empty");
    }

    @Test
    void findByTraineeCriteria_shouldReturnEmptyList_whenNonExistingUsername() {
        TraineeTrainingFilter filter = TraineeTrainingFilter.builder().username("Non-Existing Username").build();

        List<Training> actual = repository.findByTraineeCriteria(filter);

        assertThat(actual).isEmpty();
    }

    @Test
    void findByTrainerCriteria_shouldThrowException_whenNullFilter() {
        ValidationFailedException exception = assertThrows(ValidationFailedException.class,
                () -> repository.findByTrainerCriteria(null));

        assertThat(exception.getMessage()).isEqualTo("Filter cannot be null");
    }

    @Test
    void findByTrainerCriteria_shouldThrowException_whenNoUsername() {
        TrainerTrainingFilter filter = TrainerTrainingFilter.builder().build();
        ValidationFailedException exception = assertThrows(ValidationFailedException.class,
                () -> repository.findByTrainerCriteria(filter));

        assertThat(exception.getMessage()).isEqualTo("Username cannot be null or empty");
    }

    @Test
    void findByTrainerCriteria_shouldReturnEmptyList_whenNonExistingUsername() {
        TrainerTrainingFilter filter = TrainerTrainingFilter.builder().username("Non-Existing Username").build();

        List<Training> actual = repository.findByTrainerCriteria(filter);

        assertThat(actual).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("traineeFilterProviderExisting")
    void findByTraineeCriteria_shouldReturnCorrectTrainings_whenExist(TraineeTrainingFilter filter, int expectedSize, List<Long> expectedIds) {
        List<Training> actual = repository.findByTraineeCriteria(filter);

        assertThat(actual).hasSize(expectedSize);
        assertThat(actual).extracting("id").containsExactlyInAnyOrderElementsOf(expectedIds);
    }

    @ParameterizedTest
    @MethodSource("traineeFilterProviderNonExisting")
    void findByTraineeCriteria_shouldReturnCorrectTrainings_whenNotExist(TraineeTrainingFilter filter) {
        List<Training> actual = repository.findByTraineeCriteria(filter);

        assertThat(actual).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("trainerFilterProviderExisting")
    void findByTrainerCriteria_shouldReturnCorrectTrainings_whenExist(TrainerTrainingFilter filter, int expectedSize, List<Long> expectedIds) {
        List<Training> actual = repository.findByTrainerCriteria(filter);

        assertThat(actual).isNotNull().hasSize(expectedSize).extracting(Training::getId).containsExactlyInAnyOrderElementsOf(expectedIds);
    }

    @ParameterizedTest
    @MethodSource("trainerFilterProviderNonExisting")
    void findByTrainerCriteria_shouldReturnCorrectTrainings_whenNotExist(TrainerTrainingFilter filter) {
        List<Training> actual = repository.findByTrainerCriteria(filter);

        assertThat(actual).isEmpty();
    }

    private static Stream<Arguments> traineeFilterProviderExisting() {
        return Stream.of(
                Arguments.of(
                        TraineeTrainingFilter.builder()
                                .username(TRAINEE_USERNAME1)
                                .build(),
                        2,
                        List.of(20L, 21L)
                ),

                Arguments.of(
                        TraineeTrainingFilter.builder()
                                .username(TRAINEE_USERNAME1)
                                .fromDate(LocalDate.of(2024, 3, 1))
                                .toDate(LocalDate.of(2024, 3, 31))
                                .build(),
                        1,
                        List.of(20L)
                ),

                Arguments.of(
                        TraineeTrainingFilter.builder()
                                .username(TRAINEE_USERNAME1)
                                .fromDate(LocalDate.of(2024, 9, 1))
                                .toDate(LocalDate.of(2024, 9, 30))
                                .build(),
                        1,
                        List.of(21L)
                ),

                Arguments.of(
                        TraineeTrainingFilter.builder()
                                .username(TRAINEE_USERNAME1)
                                .trainingTypeName(YOGA)
                                .build(),
                        2,
                        List.of(20L, 21L)
                )
        );
    }

    private static Stream<Arguments> traineeFilterProviderNonExisting() {
        return Stream.of(
                Arguments.of(
                        TraineeTrainingFilter.builder()
                                .username(TRAINEE_USERNAME1)
                                .fromDate(LocalDate.of(2020, 1, 1))
                                .toDate(LocalDate.of(2020, 12, 31))
                                .build()
                ),

                Arguments.of(
                        TraineeTrainingFilter.builder()
                                .username(TRAINEE_USERNAME1)
                                .trainingTypeName("Cardio")
                                .build()
                ),

                Arguments.of(
                        TraineeTrainingFilter.builder()
                                .username(TRAINEE_USERNAME1)
                                .joinFullName("NonExisting Trainer")
                                .build()
                )
        );
    }

    private static Stream<Arguments> trainerFilterProviderExisting() {
        return Stream.of(
                Arguments.of(
                        TrainerTrainingFilter.builder()
                                .username(TRAINER_USERNAME)
                                .joinFullName("Alice Smith")
                                .build(),
                        2,
                        List.of(20L, 21L)
                ),

                Arguments.of(
                        TrainerTrainingFilter.builder()
                                .username(TRAINER_USERNAME)
                                .fromDate(LocalDate.of(2024, 3, 1))
                                .toDate(LocalDate.of(2024, 3, 31))
                                .build(),
                        1,
                        List.of(20L)
                ),

                Arguments.of(
                        TrainerTrainingFilter.builder()
                                .username(TRAINER_USERNAME)
                                .fromDate(LocalDate.of(2024, 9, 1))
                                .toDate(LocalDate.of(2024, 9, 30))
                                .build(),
                        1,
                        List.of(21L)
                ),

                Arguments.of(
                        TrainerTrainingFilter.builder()
                                .username(TRAINER_USERNAME)
                                .toDate(LocalDate.of(2024, 3, 31))
                                .build(),
                        1,
                        List.of(20L)
                ),

                Arguments.of(
                        TrainerTrainingFilter.builder()
                                .username(TRAINER_USERNAME)
                                .fromDate(LocalDate.of(2024, 9, 1))
                                .build(),
                        1,
                        List.of(21L)
                )
        );
    }

    private static Stream<Arguments> trainerFilterProviderNonExisting() {
        return Stream.of(
                Arguments.of(
                        TrainerTrainingFilter.builder()
                                .username(TRAINER_USERNAME)
                                .joinFullName("NonExistent")
                                .build()
                ),

                Arguments.of(
                        TrainerTrainingFilter.builder()
                                .username(TRAINER_USERNAME)
                                .fromDate(LocalDate.of(2023, 1, 1))
                                .toDate(LocalDate.of(2023, 12, 31))
                                .build()
                ),

                Arguments.of(
                        TrainerTrainingFilter.builder()
                                .username(TRAINER_USERNAME)
                                .joinFullName("NonExistent")
                                .fromDate(LocalDate.of(2023, 1, 1))
                                .build()
                )
        );
    }
}