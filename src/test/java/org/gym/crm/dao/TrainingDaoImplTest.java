package org.gym.crm.dao;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.gym.crm.model.Trainee;
import org.gym.crm.model.Trainer;
import org.gym.crm.model.Training;
import org.gym.crm.model.TrainingType;
import org.gym.crm.model.User;
import org.gym.crm.search.filter.TraineeTrainingFilter;
import org.gym.crm.search.filter.TrainerTrainingFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DatabaseSetup(value = "/dataset/training.xml")
public class TrainingDaoImplTest extends AbstractDaoTest<TrainingDao> {
    @Test
    void save_shouldSaveTraining_whenValid() {
        Training training = buildTraining();

        Training actual = dao.save(training);

        assertThat(actual.getId()).isNotNull();
        assertThat(dao.findById(actual.getId())).isPresent();
        assertThat(actual.getTrainingName()).isEqualTo("Morning Yoga");
        assertThat(actual.getTrainingDate()).isEqualTo(LocalDate.of(2026, 4, 30));
        assertThat(actual.getTrainingDuration()).isEqualTo(60);
        assertThat(actual.getTrainingType().getTrainingTypeName()).isEqualTo("Yoga");
        assertThat(actual.getTrainee().getUser().getUsername()).isEqualTo("Nora.Pemberton");
        assertThat(actual.getTrainee().getUser().getIsActive()).isTrue();
        assertThat(actual.getTrainee().getDateOfBirth()).isEqualTo(LocalDate.of(2000, 3, 10));
        assertThat(actual.getTrainee().getAddress()).isEqualTo("123 Main St");
        assertThat(actual.getTrainer().getUser().getUsername()).isEqualTo("Callum.Whitfield");
    }

    @Test
    void save_shouldThrowException_whenSavingNullTraining() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> dao.save(null));

        assertThat(exception.getMessage()).isEqualTo("Training cannot be null");
    }

    @Test
    void findById_shouldReturnTraining_whenExists() {
        Training expected = buildExpectedTraining();

        Optional<Training> actual = dao.findById(1L);

        assertThat(actual).isPresent();
        assertThat(actual.get().getTrainingName()).isEqualTo("Hot Yoga");
        assertThat(actual.get().getTrainingDate()).isEqualTo(LocalDate.of(2026, 4, 15));
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    void findById_shouldReturnEmptyOptional_whenNotFound() {
        Optional<Training> actual = dao.findById(999L);

        assertThat(actual).isEmpty();
    }

    @Test
    void findById_shouldThrowException_whenIdIsZero() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> dao.findById(0L));

        assertThat(exception.getMessage()).isEqualTo("ID must be positive and not null, got: 0");
    }

    @Test
    void findAll_shouldReturnAllTrainings_whenExist() {
        List<Training> expected = buildExpectedTrainings();

        List<Training> actual = dao.findAll();

        assertThat(actual).isNotEmpty();
        assertThat(actual).containsAll(expected);
        assertThat(actual)
                .hasSize(2)
                .extracting(Training::getTrainingName)
                .containsExactly("Hot Yoga", "Hot Yoga");
        assertThat(actual)
                .extracting(Training::getTrainingDate)
                .containsExactlyInAnyOrder(LocalDate.of(2026, 4, 20), LocalDate.of(2026, 4, 15));
        assertThat(actual)
                .extracting(Training::getTrainingDuration)
                .contains(60);
    }

    @Test
    void findByTraineeCriteria_shouldThrowException_whenNullFilter() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> dao.findByTraineeCriteria(null));

        assertThat(exception.getMessage()).isEqualTo("Filter cannot be null");
    }

    @Test
    void findByTraineeCriteria_shouldThrowException_whenNoUsername() {
        TraineeTrainingFilter filter = TraineeTrainingFilter.builder().build();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> dao.findByTraineeCriteria(filter));

        assertThat(exception.getMessage()).isEqualTo("Username cannot be null or empty");
    }

    @Test
    void findByTraineeCriteria_shouldReturnEmptyList_whenNonExistingUsername() {
        TraineeTrainingFilter filter = TraineeTrainingFilter.builder().username("Non-Existing Username").build();

        List<Training> actual = dao.findByTraineeCriteria(filter);

        assertThat(actual).isEmpty();
    }

    @Test
    void findByTrainerCriteria_shouldThrowException_whenNullFilter() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> dao.findByTrainerCriteria(null));

        assertThat(exception.getMessage()).isEqualTo("Filter cannot be null");
    }

    @Test
    void findByTrainerCriteria_shouldThrowException_whenNoUsername() {
        TrainerTrainingFilter filter = TrainerTrainingFilter.builder().build();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> dao.findByTrainerCriteria(filter));

        assertThat(exception.getMessage()).isEqualTo("Username cannot be null or empty");
    }

    @Test
    void findByTrainerCriteria_shouldReturnEmptyList_whenNonExistingUsername() {
        TrainerTrainingFilter filter = TrainerTrainingFilter.builder().username("Non-Existing Username").build();

        List<Training> actual = dao.findByTrainerCriteria(filter);

        assertThat(actual).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("traineeFilterProviderExisting")
    void findByTraineeCriteria_shouldReturnCorrectTrainings_whenExist(TraineeTrainingFilter filter, int expectedSize, List<Long> expectedIds) {
        List<Training> actual = dao.findByTraineeCriteria(filter);

        assertThat(actual).hasSize(expectedSize);
        assertThat(actual)
                .extracting(Training::getId)
                .containsExactlyInAnyOrderElementsOf(expectedIds);
    }

    @ParameterizedTest
    @MethodSource("traineeFilterProviderNonExisting")
    void findByTraineeCriteria_shouldReturnCorrectTrainings_whenNotExist(TraineeTrainingFilter filter) {
        List<Training> actual = dao.findByTraineeCriteria(filter);

        assertThat(actual).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("trainerFilterProviderExisting")
    void findByTrainerCriteria_shouldReturnCorrectTrainings_whenExist(TrainerTrainingFilter filter, int expectedSize, List<Long> expectedIds) {
        List<Training> actual = dao.findByTrainerCriteria(filter);

        assertThat(actual).hasSize(expectedSize);
        assertThat(actual)
                .extracting(Training::getId)
                .containsExactlyInAnyOrderElementsOf(expectedIds);
    }

    @ParameterizedTest
    @MethodSource("trainerFilterProviderNonExisting")
    void findByTrainerCriteria_shouldReturnCorrectTrainings_whenNotExist(TrainerTrainingFilter filter) {
        List<Training> actual = dao.findByTrainerCriteria(filter);

        assertThat(actual).isEmpty();
    }

    private static Stream<Arguments> traineeFilterProviderExisting() {
        return Stream.of(
                Arguments.of(TraineeTrainingFilter.builder()
                                .username("Nora.Pemberton")
                                .build(),
                        1,
                        List.of(1L)),
                Arguments.of(TraineeTrainingFilter.builder()
                                .username("Ellis.Hargrove")
                                .build(),
                        1,
                        List.of(2L)),
                Arguments.of(TraineeTrainingFilter.builder()
                                .username("Nora.Pemberton")
                                .fromDate(LocalDate.of(2026, 4, 1))
                                .toDate(LocalDate.of(2026, 4, 30))
                                .build(),
                        1,
                        List.of(1L)),
                Arguments.of(TraineeTrainingFilter.builder()
                                .username("Nora.Pemberton")
                                .trainingTypeName("Yoga")
                                .build(),
                        1,
                        List.of(1L)),
                Arguments.of(TraineeTrainingFilter.builder()
                                .username("Nora.Pemberton")
                                .firstName("Callum")
                                .lastName("Whitfield")
                                .build(),
                        1,
                        List.of(1L)),
                Arguments.of(TraineeTrainingFilter.builder()
                                .username("Nora.Pemberton")
                                .firstName("Callum")
                                .build(),
                        1,
                        List.of(1L)),
                Arguments.of(TraineeTrainingFilter.builder()
                                .username("Nora.Pemberton")
                                .lastName("Whitfield")
                                .build(),
                        1,
                        List.of(1L))
        );
    }

    private static Stream<Arguments> traineeFilterProviderNonExisting() {
        return Stream.of(
                Arguments.of(TraineeTrainingFilter.builder()
                        .username("Nora.Pemberton")
                        .fromDate(LocalDate.of(2020, 1, 1))
                        .toDate(LocalDate.of(2020, 12, 31))
                        .build()),
                Arguments.of(TraineeTrainingFilter.builder()
                        .username("Nora.Pemberton")
                        .trainingTypeName("Cardio")
                        .build()),
                Arguments.of(TraineeTrainingFilter.builder()
                        .username("Nora.Pemberton")
                        .trainingTypeName("Cardio")
                        .build()),
                Arguments.of(TraineeTrainingFilter.builder()
                        .username("Nora.Pemberton")
                        .firstName("Callum")
                        .lastName("Whitfield")
                        .fromDate(LocalDate.of(2026, 4, 16))
                        .toDate(LocalDate.of(2026, 4, 30))
                        .trainingTypeName("Yoga")
                        .build())
        );
    }

    private static Stream<Arguments> trainerFilterProviderExisting() {
        return Stream.of(
                Arguments.of(TrainerTrainingFilter.builder()
                                .username("Callum.Whitfield")
                                .build(),
                        2,
                        List.of(1L, 2L)),
                Arguments.of(TrainerTrainingFilter.builder()
                                .username("Callum.Whitfield")
                                .firstName("Nora")
                                .lastName("Pemberton")
                                .build(),
                        1,
                        List.of(1L)),
                Arguments.of(TrainerTrainingFilter.builder()
                                .username("Callum.Whitfield")
                                .lastName("Hargrove")
                                .build(),
                        1,
                        List.of(2L)),
                Arguments.of(TrainerTrainingFilter.builder()
                                .username("Callum.Whitfield")
                                .firstName("Ellis")
                                .lastName("Hargrove")
                                .build(),
                        1,
                        List.of(2L)),
                Arguments.of(TrainerTrainingFilter.builder()
                                .username("Callum.Whitfield")
                                .fromDate(LocalDate.of(2026, 4, 16))
                                .build(),
                        1,
                        List.of(2L)),
                Arguments.of(TrainerTrainingFilter.builder()
                                .username("Callum.Whitfield")
                                .toDate(LocalDate.of(2026, 4, 18))
                                .build(),
                        1,
                        List.of(1L)),
                Arguments.of(TrainerTrainingFilter.builder()
                                .username("Callum.Whitfield")
                                .fromDate(LocalDate.of(2026, 4, 14))
                                .toDate(LocalDate.of(2026, 4, 16))
                                .build(),
                        1,
                        List.of(1L)),
                Arguments.of(TrainerTrainingFilter.builder()
                                .username("Callum.Whitfield")
                                .firstName("Ellis")
                                .lastName("Hargrove")
                                .fromDate(LocalDate.of(2026, 4, 19))
                                .build(),
                        1,
                        List.of(2L))
        );
    }

    private static Stream<Arguments> trainerFilterProviderNonExisting() {
        return Stream.of(
                Arguments.of(TrainerTrainingFilter.builder()
                        .username("Callum.Whitfield")
                        .firstName("NonExistent")
                        .build()),
                Arguments.of(TrainerTrainingFilter.builder()
                        .username("Callum.Whitfield")
                        .fromDate(LocalDate.of(2026, 4, 21))
                        .build()),
                Arguments.of(TrainerTrainingFilter.builder()
                        .username("Callum.Whitfield")
                        .firstName("Nora")
                        .lastName("Pemberton")
                        .fromDate(LocalDate.of(2026, 4, 16))
                        .build())
        );
    }

    private Trainer buildTrainer() {
        User user = User.builder()
                .id(1L)
                .firstName("Callum")
                .lastName("Whitfield")
                .username("Callum.Whitfield")
                .password("pass111")
                .isActive(true)
                .build();

        return Trainer.builder()
                .id(1L)
                .user(user)
                .specialization(buildTrainingType())
                .build();
    }

    private Trainee buildTrainee() {
        User user = User.builder()
                .id(2L)
                .firstName("Nora")
                .lastName("Pemberton")
                .username("Nora.Pemberton")
                .password("pass222")
                .isActive(true)
                .build();

        return Trainee.builder()
                .id(1L)
                .user(user)
                .dateOfBirth(LocalDate.of(2000, 3, 10))
                .address("123 Main St")
                .build();
    }

    private TrainingType buildTrainingType() {
        return TrainingType.builder()
                .id(1L)
                .trainingTypeName("Yoga")
                .build();
    }

    private Training buildTraining() {
        return Training.builder()
                .trainingName("Morning Yoga")
                .trainingDate(LocalDate.of(2026, 4, 30))
                .trainingDuration(60)
                .trainingType(buildTrainingType())
                .trainer(buildTrainer())
                .trainee(buildTrainee())
                .build();
    }

    private Training buildExpectedTraining() {
        return Training.builder()
                .id(1L)
                .trainingName("Hot Yoga")
                .trainingDate(LocalDate.of(2026, 4, 15))
                .trainingDuration(60)
                .trainingType(buildTrainingType())
                .trainee(buildTrainee())
                .trainer(buildTrainer())
                .build();
    }

    private List<Training> buildExpectedTrainings() {
        User user = User.builder()
                .id(3L)
                .firstName("Ellis")
                .lastName("Hargrove")
                .username("Ellis.Hargrove")
                .password("pass333")
                .isActive(true)
                .build();
        Trainee trainee = Trainee.builder()
                .id(2L)
                .user(user)
                .dateOfBirth(LocalDate.of(2002, 7, 15))
                .build();
        Training training = Training.builder()
                .id(2L)
                .trainingName("Hot Yoga")
                .trainingDate(LocalDate.of(2026, 4, 20))
                .trainingDuration(60)
                .trainingType(buildTrainingType())
                .trainee(trainee)
                .trainer(buildTrainer())
                .build();

        return List.of(buildExpectedTraining(), training);
    }

    @Override
    protected Class<TrainingDao> getDaoClass() {
        return TrainingDao.class;
    }
}
