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
import org.gym.crm.service.TraineeService;
import org.gym.crm.service.TrainerService;
import org.gym.crm.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GymFacadeTest {
    private static final String FIRST_NAME = "Simone";
    private static final String LAST_NAME = "Radcliffe";
    private static final String USERNAME = "Simone.Radcliffe";
    private static final String PASSWORD = "encodedPassword";
    private static final String TRAINING_NAME = "Morning Cardio";
    private static final String TRAINING_TYPE_NAME = "Cardio";
    private static final long VALID_ID = 1L;

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
    void deleteTrainee_shouldDeleteTrainee() {
        facade.deleteTrainee(VALID_ID);

        verify(traineeService).delete(VALID_ID);
    }

    @Test
    void getTraineeById_shouldReturnTraineeResponseDTO_whenExists() {
        when(traineeService.findById(VALID_ID)).thenReturn(Optional.ofNullable(trainee));
        when(traineeMapper.toDto(trainee)).thenReturn(traineeResponseDTO);

        TraineeResponseDTO actual = facade.getTraineeById(VALID_ID);

        assertEquals(traineeResponseDTO, actual);
        verify(traineeService).findById(VALID_ID);
        verify(traineeMapper).toDto(trainee);
    }

    @Test
    void getAllTrainees_shouldReturnAllTraineesAsResponseDTOs_whenExist() {
        when(traineeService.findAll()).thenReturn(List.of(trainee));
        when(traineeMapper.toDto(trainee)).thenReturn(traineeResponseDTO);

        List<TraineeResponseDTO> actual = facade.getAllTrainees();

        assertEquals(1, actual.size());
        assertEquals(traineeResponseDTO, actual.getFirst());
        verify(traineeService).findAll();
    }

    @Test
    void getAllTrainees_shouldReturnEmptyList_whenNoTrainees() {
        when(traineeService.findAll()).thenReturn(List.of());

        List<TraineeResponseDTO> actual = facade.getAllTrainees();

        assertTrue(actual.isEmpty());
        verify(traineeService).findAll();
        verify(traineeMapper, never()).toDto(trainee);
    }

    @Test
    void createTrainer_shouldSaveAndReturnResponseDTO() {
        Trainer saved = trainer.toBuilder()
                .user(
                        trainer.getUser().toBuilder()
                                .id(2L)
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
                                .id(2L)
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
    void getTrainerById_shouldReturnTrainerResponseDTO_whenExists() {
        when(trainerService.findById(2L)).thenReturn(Optional.ofNullable(trainer));
        when(trainerMapper.toDto(trainer)).thenReturn(trainerResponseDTO);

        TrainerResponseDTO actual = facade.getTrainerById(2L);

        assertEquals(trainerResponseDTO, actual);
        verify(trainerService).findById(2L);
        verify(trainerMapper).toDto(trainer);
    }

    @Test
    void getAllTrainers_shouldReturnAllTrainersAsResponseDTOs_whenExist() {
        when(trainerService.findAll()).thenReturn(List.of(trainer));
        when(trainerMapper.toDto(trainer)).thenReturn(trainerResponseDTO);

        List<TrainerResponseDTO> actual = facade.getAllTrainers();

        assertEquals(1, actual.size());
        assertEquals(trainerResponseDTO, actual.getFirst());
        verify(trainerService).findAll();
    }

    @Test
    void getAllTrainers_shouldReturnEmptyList_whenNoTrainers() {
        when(trainerService.findAll()).thenReturn(List.of());

        List<TrainerResponseDTO> actual = facade.getAllTrainers();

        assertTrue(actual.isEmpty());
        verify(trainerService).findAll();
        verify(trainerMapper, never()).toDto(trainer);
    }

    @Test
    void createTraining_shouldSaveAndReturnResponseDTO() {
        when(trainingMapper.toEntity(trainingRequestDTO)).thenReturn(training);
        when(trainingService.create(training)).thenReturn(training);
        when(trainingMapper.toDto(training)).thenReturn(trainingResponseDTO);

        TrainingResponseDTO actual = facade.createTraining(trainingRequestDTO);

        assertEquals(trainingResponseDTO, actual);
        verify(trainingMapper).toEntity(trainingRequestDTO);
        verify(trainingService).create(training);
        verify(trainingMapper).toDto(training);
    }

    @Test
    void getTrainingById_shouldReturnResponseDTO_whenExists() {
        when(trainingService.findById(VALID_ID)).thenReturn(Optional.ofNullable(training));
        when(trainingMapper.toDto(training)).thenReturn(trainingResponseDTO);

        TrainingResponseDTO result = facade.getTrainingById(VALID_ID);

        assertEquals(trainingResponseDTO, result);
        verify(trainingService).findById(VALID_ID);
        verify(trainingMapper).toDto(training);
    }

    @Test
    void getAllTrainings_shouldReturnAllTrainingsAsResponseDTOs_whenExist() {
        when(trainingService.findAll()).thenReturn(List.of(training));
        when(trainingMapper.toDto(training)).thenReturn(trainingResponseDTO);

        List<TrainingResponseDTO> actual = facade.getAllTrainings();

        assertEquals(1, actual.size());
        assertEquals(trainingResponseDTO, actual.getFirst());
        verify(trainingService).findAll();
        verify(trainingMapper).toDto(training);
    }

    @Test
    void getAllTrainings_shouldReturnEmptyList_whenNoTrainings() {
        when(trainingService.findAll()).thenReturn(List.of());

        List<TrainingResponseDTO> actual = facade.getAllTrainings();

        assertTrue(actual.isEmpty());
        verify(trainingService).findAll();
        verify(trainingMapper, never()).toDto(training);
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
                .userId(2L)
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
