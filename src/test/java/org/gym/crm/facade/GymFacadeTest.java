package org.gym.crm.facade;

import org.gym.crm.dto.TraineeRequestDTO;
import org.gym.crm.dto.TraineeResponseDTO;
import org.gym.crm.dto.TraineeUpdateDTO;
import org.gym.crm.dto.TrainerRequestDTO;
import org.gym.crm.dto.TrainerResponseDTO;
import org.gym.crm.dto.TrainerUpdateDTO;
import org.gym.crm.dto.TrainingRequestDTO;
import org.gym.crm.dto.TrainingResponseDTO;
import org.gym.crm.mapper.TraineeMapper;
import org.gym.crm.mapper.TrainerMapper;
import org.gym.crm.mapper.TrainingMapper;
import org.gym.crm.model.Trainee;
import org.gym.crm.model.Trainer;
import org.gym.crm.model.Training;
import org.gym.crm.model.TrainingType;
import org.gym.crm.model.User;
import org.gym.crm.search.filter.TraineeTrainingFilter;
import org.gym.crm.search.filter.TrainerTrainingFilter;
import org.gym.crm.service.TraineeService;
import org.gym.crm.service.TrainerService;
import org.gym.crm.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.naming.AuthenticationException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GymFacadeTest {
    private static final String FIRST_NAME = "Simone";
    private static final String LAST_NAME = "Radcliffe";
    private static final String USERNAME = "Simone.Radcliffe";
    private static final String PASSWORD = "encodedPassword";
    private static final String NEW_PASSWORD = "newPassword";
    private static final String OLD_PASSWORD = "oldPassword";
    private static final String TRAINING_NAME = "Morning Cardio";
    private static final String TRAINING_TYPE_NAME = "Cardio";
    private static final long VALID_ID = 1L;
    private static final long TRAINER_ID = 2L;

    @Mock
    private TraineeService traineeService;
    @Mock
    private TrainerService trainerService;
    @Mock
    private TrainingService trainingService;
    @Mock
    private TraineeMapper traineeMapper;
    @Mock
    private TrainerMapper trainerMapper;
    @Mock
    private TrainingMapper trainingMapper;

    private GymFacade facade;
    private Trainee trainee;
    private Trainer trainer;
    private Training training;
    private TraineeRequestDTO traineeRequestDTO;
    private TraineeUpdateDTO traineeUpdateDTO;
    private TraineeResponseDTO traineeResponseDTO;
    private TrainerRequestDTO trainerRequestDTO;
    private TrainerUpdateDTO trainerUpdateDTO;
    private TrainerResponseDTO trainerResponseDTO;
    private TrainingRequestDTO trainingRequestDTO;
    private TrainingResponseDTO trainingResponseDTO;

    @BeforeEach
    void setUp() {
        facade = new GymFacade(traineeService, trainerService, trainingService);
        facade.setTraineeMapper(traineeMapper);
        facade.setTrainerMapper(trainerMapper);
        facade.setTrainingMapper(trainingMapper);

        trainee = buildTrainee();
        traineeRequestDTO = buildTraineeRequestDTO();
        traineeUpdateDTO = buildTraineeUpdateDTO();
        traineeResponseDTO = buildTraineeResponseDTO();
        trainer = buildTrainer();
        trainerRequestDTO = buildTrainerRequestDTO();
        trainerUpdateDTO = buildTrainerUpdateDTO();
        trainerResponseDTO = buildTrainerResponseDTO();
        training = buildTraining();
        trainingRequestDTO = buildTrainingRequestDTO();
        trainingResponseDTO = buildTrainingResponseDTO();
    }

    @Test
    void createTrainee_shouldSaveAndReturnResponseDTO() {
        Trainee saved = trainee.toBuilder()
                .id(VALID_ID)
                .user(
                        trainee.getUser().toBuilder()
                                .id(VALID_ID)
                                .username(USERNAME)
                                .password(PASSWORD)
                                .isActive(true)
                                .build()
                )
                .build();

        when(traineeMapper.toEntity(traineeRequestDTO)).thenReturn(trainee);
        when(traineeService.create(trainee)).thenReturn(saved);
        when(traineeMapper.toDto(saved)).thenReturn(traineeResponseDTO);

        TraineeResponseDTO actual = facade.createTrainee(traineeRequestDTO);

        assertEquals(traineeResponseDTO, actual);
        verify(traineeMapper).toEntity(traineeRequestDTO);
        verify(traineeService).create(trainee);
        verify(traineeMapper).toDto(saved);
    }

    @Test
    void updateTrainee_shouldSaveAndReturnResponseDTO() {
        Trainee saved = trainee.toBuilder()
                .id(VALID_ID)
                .user(
                        trainee.getUser().toBuilder()
                                .id(VALID_ID)
                                .username(USERNAME)
                                .password(PASSWORD)
                                .isActive(true)
                                .build()
                )
                .build();

        when(traineeMapper.toEntity(traineeUpdateDTO)).thenReturn(saved);
        when(traineeService.update(saved)).thenReturn(saved);
        when(traineeMapper.toDto(saved)).thenReturn(traineeResponseDTO);

        TraineeResponseDTO actual = facade.updateTrainee(traineeUpdateDTO);

        assertEquals(traineeResponseDTO, actual);
        verify(traineeMapper).toEntity(traineeUpdateDTO);
        verify(traineeService).update(saved);
        verify(traineeMapper).toDto(saved);
    }

    @Test
    void createTrainer_shouldSaveAndReturnResponseDTO() {
        Trainer saved = trainer.toBuilder()
                .user(
                        trainer.getUser().toBuilder()
                                .id(TRAINER_ID)
                                .username(USERNAME)
                                .password(PASSWORD)
                                .isActive(true)
                                .build()
                )
                .build();

        when(trainerMapper.toEntity(trainerRequestDTO)).thenReturn(trainer);
        when(trainerService.create(trainer)).thenReturn(saved);
        when(trainerMapper.toDto(saved)).thenReturn(trainerResponseDTO);

        TrainerResponseDTO actual = facade.createTrainer(trainerRequestDTO);

        assertEquals(trainerResponseDTO, actual);
        verify(trainerMapper).toEntity(trainerRequestDTO);
        verify(trainerService).create(trainer);
        verify(trainerMapper).toDto(saved);
    }

    @Test
    void updateTrainer_shouldSaveAndReturnResponseDTO() {
        Trainer saved = trainer.toBuilder()
                .user(
                        trainer.getUser().toBuilder()
                                .id(TRAINER_ID)
                                .username(USERNAME)
                                .password(PASSWORD)
                                .isActive(true)
                                .build()
                )
                .build();

        when(trainerMapper.toEntity(trainerUpdateDTO)).thenReturn(saved);
        when(trainerService.update(any(Trainer.class))).thenReturn(saved);
        when(trainerMapper.toDto(saved)).thenReturn(trainerResponseDTO);

        TrainerResponseDTO actual = facade.updateTrainer(trainerUpdateDTO);

        assertEquals(trainerResponseDTO, actual);
        verify(trainerMapper).toEntity(trainerUpdateDTO);
        verify(trainerService).update(any(Trainer.class));
        verify(trainerMapper).toDto(saved);
    }

    @Test
    void createTraining_shouldSaveAndReturnResponseDTO() {
        when(trainingMapper.toEntity(trainingRequestDTO)).thenReturn(training);
        when(trainingService.create(training)).thenReturn(training);
        when(trainingMapper.toDto(training)).thenReturn(trainingResponseDTO);

        TrainingResponseDTO actual = facade.createTraining(USERNAME, PASSWORD, trainingRequestDTO);

        assertEquals(trainingResponseDTO, actual);
        verify(trainingMapper).toEntity(trainingRequestDTO);
        verify(trainingService).create(training);
        verify(trainingMapper).toDto(training);
    }

    @Test
    void updateTrainee_withAuthentication_shouldAuthenticateAndUpdateAndReturnResponseDTO() {
        Trainee updatedTrainee = trainee.toBuilder()
                .id(VALID_ID)
                .user(trainee.getUser().toBuilder().id(VALID_ID).build())
                .build();

        when(traineeService.authenticate(USERNAME, PASSWORD)).thenReturn(true);
        when(traineeMapper.toEntity(traineeUpdateDTO)).thenReturn(updatedTrainee);
        when(traineeService.updateProfile(USERNAME, updatedTrainee)).thenReturn(updatedTrainee);
        when(traineeMapper.toDto(updatedTrainee)).thenReturn(traineeResponseDTO);

        TraineeResponseDTO actual = facade.updateTrainee(USERNAME, PASSWORD, traineeUpdateDTO);

        assertEquals(traineeResponseDTO, actual);
        verify(traineeService).authenticate(USERNAME, PASSWORD);
        verify(traineeMapper).toEntity(traineeUpdateDTO);
        verify(traineeService).updateProfile(USERNAME, updatedTrainee);
        verify(traineeMapper).toDto(updatedTrainee);
    }

    @Test
    void setTraineeActive_shouldAuthenticateAndSetActiveTrue() {
        when(traineeService.authenticate(USERNAME, PASSWORD)).thenReturn(true);

        facade.setTraineeActive(USERNAME, PASSWORD, true);

        verify(traineeService).authenticate(USERNAME, PASSWORD);
        verify(traineeService).setActive(USERNAME, true);
    }

    @Test
    void setTraineeActive_shouldAuthenticateAndSetActiveFalse() {
        when(traineeService.authenticate(USERNAME, PASSWORD)).thenReturn(true);

        facade.setTraineeActive(USERNAME, PASSWORD, false);

        verify(traineeService).authenticate(USERNAME, PASSWORD);
        verify(traineeService).setActive(USERNAME, false);
    }

    @Test
    void setTrainerActive_shouldAuthenticateAndSetActiveTrue() {
        when(trainerService.authenticate(USERNAME, PASSWORD)).thenReturn(true);

        facade.setTrainerActive(USERNAME, PASSWORD, true);

        verify(trainerService).authenticate(USERNAME, PASSWORD);
        verify(trainerService).setActive(USERNAME, true);
    }

    @Test
    void setTrainerActive_shouldAuthenticateAndSetActiveFalse() {
        when(trainerService.authenticate(USERNAME, PASSWORD)).thenReturn(true);

        facade.setTrainerActive(USERNAME, PASSWORD, false);

        verify(trainerService).authenticate(USERNAME, PASSWORD);
        verify(trainerService).setActive(USERNAME, false);
    }

    @Test
    void deleteTraineeByUsername_shouldAuthenticateAndDelete() {
        when(traineeService.authenticate(USERNAME, PASSWORD)).thenReturn(true);

        facade.deleteTraineeByUsername(USERNAME, PASSWORD);

        verify(traineeService).authenticate(USERNAME, PASSWORD);
        verify(traineeService).deleteByUsername(USERNAME);
    }

    @Test
    void getTraineeTrainings_shouldAuthenticateAndReturnTrainings() {
        TraineeTrainingFilter filter = TraineeTrainingFilter.builder().build();
        List<Training> trainings = List.of(training);

        when(traineeService.authenticate(USERNAME, PASSWORD)).thenReturn(true);
        when(traineeService.getTrainings(filter)).thenReturn(trainings);
        when(trainingMapper.toDto(training)).thenReturn(trainingResponseDTO);

        List<TrainingResponseDTO> actual = facade.getTraineeTrainings(USERNAME, PASSWORD, filter);

        assertEquals(1, actual.size());
        assertEquals(trainingResponseDTO, actual.getFirst());
        verify(traineeService).authenticate(USERNAME, PASSWORD);
        verify(traineeService).getTrainings(filter);
        verify(trainingMapper).toDto(training);
    }

    @Test
    void getTraineeTrainings_shouldReturnEmptyListWhenNoTrainings() {
        TraineeTrainingFilter filter = TraineeTrainingFilter.builder().build();

        when(traineeService.authenticate(USERNAME, PASSWORD)).thenReturn(true);
        when(traineeService.getTrainings(filter)).thenReturn(List.of());

        List<TrainingResponseDTO> actual = facade.getTraineeTrainings(USERNAME, PASSWORD, filter);

        assertTrue(actual.isEmpty());
        verify(traineeService).authenticate(USERNAME, PASSWORD);
        verify(traineeService).getTrainings(filter);
        verify(trainingMapper, never()).toDto(training);
    }

    @Test
    void getTrainerTrainings_shouldAuthenticateAndReturnTrainings() {
        TrainerTrainingFilter filter = TrainerTrainingFilter.builder().build();
        List<Training> trainings = List.of(training);

        when(trainerService.authenticate(USERNAME, PASSWORD)).thenReturn(true);
        when(trainerService.getTrainings(filter)).thenReturn(trainings);
        when(trainingMapper.toDto(training)).thenReturn(trainingResponseDTO);

        List<TrainingResponseDTO> actual = facade.getTrainerTrainings(USERNAME, PASSWORD, filter);

        assertEquals(1, actual.size());
        assertEquals(trainingResponseDTO, actual.getFirst());
        verify(trainerService).authenticate(USERNAME, PASSWORD);
        verify(trainerService).getTrainings(filter);
        verify(trainingMapper).toDto(training);
    }

    @Test
    void getTrainerTrainings_shouldReturnEmptyListWhenNoTrainings() {
        TrainerTrainingFilter filter = TrainerTrainingFilter.builder().build();

        when(trainerService.authenticate(USERNAME, PASSWORD)).thenReturn(true);
        when(trainerService.getTrainings(filter)).thenReturn(List.of());

        List<TrainingResponseDTO> actual = facade.getTrainerTrainings(USERNAME, PASSWORD, filter);

        assertTrue(actual.isEmpty());
        verify(trainerService).authenticate(USERNAME, PASSWORD);
        verify(trainerService).getTrainings(filter);
        verify(trainingMapper, never()).toDto(training);
    }

    @Test
    void createTraining_withAuthentication_shouldAuthenticateAndCreateAndReturnResponseDTO() {
        when(traineeService.authenticate(USERNAME, PASSWORD)).thenReturn(true);
        when(trainingMapper.toEntity(trainingRequestDTO)).thenReturn(training);
        when(trainingService.create(training)).thenReturn(training);
        when(trainingMapper.toDto(training)).thenReturn(trainingResponseDTO);

        TrainingResponseDTO actual = facade.createTraining(USERNAME, PASSWORD, trainingRequestDTO);

        assertEquals(trainingResponseDTO, actual);
        verify(traineeService).authenticate(USERNAME, PASSWORD);
        verify(trainingMapper).toEntity(trainingRequestDTO);
        verify(trainingService).create(training);
        verify(trainingMapper).toDto(training);
    }

    @Test
    void getUnassignedTrainers_shouldAuthenticateAndReturnTrainers() {
        when(traineeService.authenticate(USERNAME, PASSWORD)).thenReturn(true);
        when(traineeService.getUnassignedTrainers(USERNAME)).thenReturn(List.of(trainer));
        when(trainerMapper.toDto(trainer)).thenReturn(trainerResponseDTO);

        List<TrainerResponseDTO> actual = facade.getUnassignedTrainers(USERNAME, PASSWORD);

        assertEquals(1, actual.size());
        assertEquals(trainerResponseDTO, actual.getFirst());
        verify(traineeService).authenticate(USERNAME, PASSWORD);
        verify(traineeService).getUnassignedTrainers(USERNAME);
        verify(trainerMapper).toDto(trainer);
    }

    @Test
    void getUnassignedTrainers_shouldReturnEmptyListWhenNoUnassignedTrainers() {
        when(traineeService.authenticate(USERNAME, PASSWORD)).thenReturn(true);
        when(traineeService.getUnassignedTrainers(USERNAME)).thenReturn(List.of());

        List<TrainerResponseDTO> actual = facade.getUnassignedTrainers(USERNAME, PASSWORD);

        assertTrue(actual.isEmpty());
        verify(traineeService).authenticate(USERNAME, PASSWORD);
        verify(traineeService).getUnassignedTrainers(USERNAME);
        verify(trainerMapper, never()).toDto(trainer);
    }

    @Test
    void updateTraineeTrainers_shouldAuthenticateAndUpdateAndReturnResponseDTO() {
        List<String> trainerUsernames = List.of("trainer1", "trainer2");
        Trainee updatedTrainee = trainee.toBuilder().id(VALID_ID).build();

        when(traineeService.authenticate(USERNAME, PASSWORD)).thenReturn(true);
        when(traineeService.updateTrainers(USERNAME, trainerUsernames)).thenReturn(updatedTrainee);
        when(traineeMapper.toDto(updatedTrainee)).thenReturn(traineeResponseDTO);

        TraineeResponseDTO actual = facade.updateTraineeTrainers(USERNAME, PASSWORD, trainerUsernames);

        assertEquals(traineeResponseDTO, actual);
        verify(traineeService).authenticate(USERNAME, PASSWORD);
        verify(traineeService).updateTrainers(USERNAME, trainerUsernames);
        verify(traineeMapper).toDto(updatedTrainee);
    }

    @Test
    void getTrainerByUsername_shouldReturnTrainerResponseDTO_whenExists() {
        when(trainerService.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
        when(trainerMapper.toDto(trainer)).thenReturn(trainerResponseDTO);

        TrainerResponseDTO actual = facade.getTrainerByUsername(USERNAME, PASSWORD);

        assertEquals(trainerResponseDTO, actual);
        verify(trainerService).findByUsername(USERNAME);
        verify(trainerMapper).toDto(trainer);
    }

    @Test
    void getTraineeByUsername_shouldReturnTraineeResponseDTO_whenExists() {
        when(traineeService.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(traineeMapper.toDto(trainee)).thenReturn(traineeResponseDTO);

        TraineeResponseDTO actual = facade.getTraineeByUsername(USERNAME, PASSWORD);

        assertEquals(traineeResponseDTO, actual);
        verify(traineeService).findByUsername(USERNAME);
        verify(traineeMapper).toDto(trainee);
    }

    @Test
    void changeTraineePassword_shouldCallServiceMethod() throws AuthenticationException {
        facade.changeTraineePassword(USERNAME, OLD_PASSWORD, NEW_PASSWORD);

        verify(traineeService).changePassword(USERNAME, OLD_PASSWORD, NEW_PASSWORD);
    }

    @Test
    void changeTrainerPassword_shouldCallServiceMethod() throws AuthenticationException {
        facade.changeTrainerPassword(USERNAME, OLD_PASSWORD, NEW_PASSWORD);

        verify(trainerService).changePassword(USERNAME, OLD_PASSWORD, NEW_PASSWORD);
    }

    @Test
    void updateTrainer_withAuthentication_shouldAuthenticateAndUpdateAndReturnResponseDTO() {
        Trainer updatedTrainer = trainer.toBuilder()
                .user(trainer.getUser().toBuilder().id(TRAINER_ID).build())
                .build();

        when(trainerService.authenticate(USERNAME, PASSWORD)).thenReturn(true);
        when(trainerMapper.toEntity(trainerUpdateDTO)).thenReturn(updatedTrainer);
        when(trainerService.updateProfile(USERNAME, updatedTrainer)).thenReturn(updatedTrainer);
        when(trainerMapper.toDto(updatedTrainer)).thenReturn(trainerResponseDTO);

        TrainerResponseDTO actual = facade.updateTrainer(USERNAME, PASSWORD, trainerUpdateDTO);

        assertEquals(trainerResponseDTO, actual);
        verify(trainerService).authenticate(USERNAME, PASSWORD);
        verify(trainerMapper).toEntity(trainerUpdateDTO);
        verify(trainerService).updateProfile(USERNAME, updatedTrainer);
        verify(trainerMapper).toDto(updatedTrainer);
    }

    private Trainee buildTrainee() {
        return Trainee.builder()
                .dateOfBirth(LocalDate.of(1980, 1, 1))
                .address("123 Oak St")
                .user(
                        User.builder()
                                .firstName(FIRST_NAME)
                                .lastName(LAST_NAME)
                                .username(USERNAME)
                                .password(PASSWORD)
                                .isActive(true)
                                .build()
                )
                .build();
    }

    private TraineeRequestDTO buildTraineeRequestDTO() {
        return TraineeRequestDTO.builder()
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .build();
    }

    private TraineeUpdateDTO buildTraineeUpdateDTO() {
        return TraineeUpdateDTO.builder()
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .build();
    }

    private TraineeResponseDTO buildTraineeResponseDTO() {
        return TraineeResponseDTO.builder()
                .userId(VALID_ID)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .username(USERNAME)
                .isActive(true)
                .build();
    }

    private Trainer buildTrainer() {
        return Trainer.builder()
                .specialization(
                        TrainingType.builder()
                                .trainingTypeName(TRAINING_TYPE_NAME)
                                .build()
                )
                .user(
                        User.builder()
                                .firstName(FIRST_NAME)
                                .lastName(LAST_NAME)
                                .username(USERNAME)
                                .password(PASSWORD)
                                .isActive(true)
                                .build()
                )
                .build();
    }

    private TrainerRequestDTO buildTrainerRequestDTO() {
        return TrainerRequestDTO.builder()
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .build();
    }

    private TrainerUpdateDTO buildTrainerUpdateDTO() {
        return TrainerUpdateDTO.builder()
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .build();
    }

    private TrainerResponseDTO buildTrainerResponseDTO() {
        return TrainerResponseDTO.builder()
                .userId(TRAINER_ID)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .username(USERNAME)
                .isActive(true)
                .build();
    }

    private Training buildTraining() {
        return Training.builder()
                .id(VALID_ID)
                .trainingName(TRAINING_NAME)
                .trainingType(
                        TrainingType.builder()
                                .trainingTypeName(TRAINING_TYPE_NAME)
                                .build()
                )
                .trainingDate(LocalDate.of(2024, 1, 15))
                .trainingDuration(60)
                .trainee(
                        Trainee.builder()
                                .id(VALID_ID)
                                .build()
                )
                .trainer(
                        Trainer.builder()
                                .id(VALID_ID)
                                .build()
                )
                .build();
    }

    private TrainingRequestDTO buildTrainingRequestDTO() {
        return TrainingRequestDTO.builder()
                .traineeId(VALID_ID)
                .trainerId(VALID_ID)
                .trainingName(TRAINING_NAME)
                .trainingTypeName(TRAINING_TYPE_NAME)
                .trainingDate(LocalDate.of(2024, 1, 15))
                .trainingDuration(60)
                .build();
    }

    private TrainingResponseDTO buildTrainingResponseDTO() {
        return TrainingResponseDTO.builder()
                .id(VALID_ID)
                .traineeId(VALID_ID)
                .trainerId(VALID_ID)
                .trainingName(TRAINING_NAME)
                .trainingTypeName(TRAINING_TYPE_NAME)
                .trainingDate(LocalDate.of(2024, 1, 15))
                .trainingDuration(60)
                .build();
    }
}