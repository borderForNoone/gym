package org.gym.crm.facade;

import org.gym.crm.dto.PasswordChangeRequest;
import org.gym.crm.dto.ToggleActiveRequestDTO;
import org.gym.crm.dto.TraineeInfoDTO;
import org.gym.crm.dto.TraineeRequestDTO;
import org.gym.crm.dto.TraineeResponseDTO;
import org.gym.crm.dto.TraineeUpdateDTO;
import org.gym.crm.dto.TrainerAssignmentUpdateDTO;
import org.gym.crm.dto.TrainerInfoDTO;
import org.gym.crm.dto.TrainerRequestDTO;
import org.gym.crm.dto.TrainerResponseDTO;
import org.gym.crm.dto.TrainingRequestDTO;
import org.gym.crm.dto.TrainingResponseDTO;
import org.gym.crm.mapper.TraineeMapper;
import org.gym.crm.mapper.TraineeRestMapper;
import org.gym.crm.mapper.TrainerMapper;
import org.gym.crm.mapper.TrainerRestMapper;
import org.gym.crm.mapper.TrainingMapper;
import org.gym.crm.model.Trainee;
import org.gym.crm.model.Trainer;
import org.gym.crm.model.Training;
import org.gym.crm.model.TrainingType;
import org.gym.crm.model.User;
import org.gym.crm.rest.ActivationStatusRequest;
import org.gym.crm.rest.AssignedTrainerResponse;
import org.gym.crm.rest.LoginChangeRequest;
import org.gym.crm.rest.LoginRequest;
import org.gym.crm.rest.TraineeAssignedTrainersUpdateRequest;
import org.gym.crm.rest.TraineeAssignedTrainersUpdateResponse;
import org.gym.crm.rest.TraineeGetResponse;
import org.gym.crm.rest.TraineeUpdateRequest;
import org.gym.crm.rest.TraineeUpdateResponse;
import org.gym.crm.rest.TrainerCreateRequest;
import org.gym.crm.rest.TrainerCreateResponse;
import org.gym.crm.rest.TrainerGetResponse;
import org.gym.crm.search.filter.TraineeTrainingFilter;
import org.gym.crm.search.filter.TrainerTrainingFilter;
import org.gym.crm.service.TraineeService;
import org.gym.crm.service.TrainerService;
import org.gym.crm.service.TrainingService;
import org.gym.crm.service.UserProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
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
    private UserProfileService userProfileService;
    @Mock
    private TraineeRestMapper traineeRestMapper;
    @Mock
    private TrainerRestMapper trainerRestMapper;
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
    private TraineeResponseDTO traineeResponseDTO;
    private TrainerRequestDTO trainerRequestDTO;
    private TrainerResponseDTO trainerResponseDTO;
    private TrainingRequestDTO trainingRequestDTO;
    private TrainingResponseDTO trainingResponseDTO;

    @BeforeEach
    void setUp() {
        facade = new GymFacade(
                traineeService, trainerService, trainingService,
                userProfileService, traineeRestMapper, trainerRestMapper
        );
        facade.setTraineeMapper(traineeMapper);
        facade.setTrainerMapper(trainerMapper);
        facade.setTrainingMapper(trainingMapper);

        trainee = buildTrainee();
        traineeRequestDTO = buildTraineeRequestDTO();
        traineeResponseDTO = buildTraineeResponseDTO();
        trainer = buildTrainer();
        trainerRequestDTO = buildTrainerRequestDTO();
        trainerResponseDTO = buildTrainerResponseDTO();
        training = buildTraining();
        trainingRequestDTO = buildTrainingRequestDTO();
        trainingResponseDTO = buildTrainingResponseDTO();
    }

    @Test
    void createTrainee_shouldSaveAndReturnResponseDTO() {
        Trainee saved = trainee.toBuilder()
                .id(VALID_ID)
                .user(trainee.getUser().toBuilder()
                        .id(VALID_ID).username(USERNAME).password(PASSWORD).isActive(true).build())
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
    void updateTrainee_shouldMapAndReturnUpdateResponse() {
        TraineeUpdateRequest request = new TraineeUpdateRequest();
        TraineeUpdateDTO dto = TraineeUpdateDTO.builder().firstName(FIRST_NAME).lastName(LAST_NAME).build();
        TraineeResponseDTO responseDTO = buildTraineeResponseDTO();
        TraineeUpdateResponse updateResponse = new TraineeUpdateResponse();

        when(traineeRestMapper.toDto(USERNAME, request)).thenReturn(dto);
        when(traineeService.update(dto)).thenReturn(responseDTO);
        when(traineeRestMapper.toRestUpdateResponse(responseDTO)).thenReturn(updateResponse);

        TraineeUpdateResponse actual = facade.updateTrainee(request, USERNAME);

        assertEquals(updateResponse, actual);
        verify(traineeRestMapper).toDto(USERNAME, request);
        verify(traineeService).update(dto);
        verify(traineeRestMapper).toRestUpdateResponse(responseDTO);
    }

    @Test
    void getTraineeByUsername_shouldReturnMappedResponse() {
        TraineeInfoDTO infoDTO = TraineeInfoDTO.builder().build();
        TraineeGetResponse restResponse = new TraineeGetResponse();

        when(traineeService.getTraineeByUsername(USERNAME)).thenReturn(infoDTO);
        when(traineeRestMapper.toRest(infoDTO)).thenReturn(restResponse);

        TraineeGetResponse actual = facade.getTraineeByUsername(USERNAME);

        assertEquals(restResponse, actual);
        verify(traineeService).getTraineeByUsername(USERNAME);
        verify(traineeRestMapper).toRest(infoDTO);
    }

    @Test
    void deleteTraineeByUsername_shouldDelegate() {
        facade.deleteTraineeByUsername(USERNAME);

        verify(traineeService).deleteByUsername(USERNAME);
    }

    @Test
    void setTraineeActive_shouldDelegate() {
        facade.setTraineeActive(USERNAME, true);

        verify(traineeService).setActive(USERNAME, true);
    }

    @Test
    void setTrainerActive_shouldDelegate() {
        facade.setTrainerActive(USERNAME, false);

        verify(trainerService).setActive(USERNAME, false);
    }

    @Test
    void toggleActiveStatus_shouldBuildDtoAndDelegate() {
        ActivationStatusRequest request = new ActivationStatusRequest();
        request.setIsActive(true);

        facade.toggleActiveStatus(request, USERNAME);

        verify(userProfileService).toggleActive(ToggleActiveRequestDTO.builder()
                .username(USERNAME)
                .isActive(true)
                .build());
    }

    @Test
    void getTraineeTrainings_shouldReturnMappedList() {
        TraineeTrainingFilter filter = TraineeTrainingFilter.builder().build();

        when(traineeService.getTrainings(filter)).thenReturn(List.of(training));
        when(trainingMapper.toDto(training)).thenReturn(trainingResponseDTO);

        List<TrainingResponseDTO> actual = facade.getTraineeTrainings(filter);

        assertEquals(1, actual.size());
        assertEquals(trainingResponseDTO, actual.getFirst());
    }

    @Test
    void getTraineeTrainings_shouldReturnEmptyListWhenNoTrainings() {
        TraineeTrainingFilter filter = TraineeTrainingFilter.builder().build();

        when(traineeService.getTrainings(filter)).thenReturn(List.of());

        List<TrainingResponseDTO> actual = facade.getTraineeTrainings(filter);

        assertTrue(actual.isEmpty());
        verify(trainingMapper, never()).toDto(any());
    }

    @Test
    void getTrainerTrainings_shouldReturnMappedList() {
        TrainerTrainingFilter filter = TrainerTrainingFilter.builder().build();

        when(trainerService.getTrainings(filter)).thenReturn(List.of(training));
        when(trainingMapper.toDto(training)).thenReturn(trainingResponseDTO);

        List<TrainingResponseDTO> actual = facade.getTrainerTrainings(filter);

        assertEquals(1, actual.size());
        assertEquals(trainingResponseDTO, actual.getFirst());
    }

    @Test
    void getTrainerTrainings_shouldReturnEmptyListWhenNoTrainings() {
        TrainerTrainingFilter filter = TrainerTrainingFilter.builder().build();

        when(trainerService.getTrainings(filter)).thenReturn(List.of());

        List<TrainingResponseDTO> actual = facade.getTrainerTrainings(filter);

        assertTrue(actual.isEmpty());
        verify(trainingMapper, never()).toDto(any());
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
    void getUnassignedTrainers_shouldReturnMappedList() {
        when(traineeService.getUnassignedTrainers(USERNAME)).thenReturn(List.of(trainer));
        when(trainerMapper.toDto(trainer)).thenReturn(trainerResponseDTO);

        List<TrainerResponseDTO> actual = facade.getUnassignedTrainers(USERNAME);

        assertEquals(1, actual.size());
        assertEquals(trainerResponseDTO, actual.getFirst());
    }

    @Test
    void getUnassignedTrainers_shouldReturnEmptyList() {
        when(traineeService.getUnassignedTrainers(USERNAME)).thenReturn(List.of());

        List<TrainerResponseDTO> actual = facade.getUnassignedTrainers(USERNAME);

        assertTrue(actual.isEmpty());
        verify(trainerMapper, never()).toDto(any());
    }

    @Test
    void updateTraineeTrainersList_shouldBuildDtoAndReturnResponse() {
        TraineeAssignedTrainersUpdateRequest request = new TraineeAssignedTrainersUpdateRequest();
        request.setTrainerUsernames(List.of("trainer1", "trainer2"));

        TrainerInfoDTO trainerInfoDTO = TrainerInfoDTO.builder().build();
        AssignedTrainerResponse assignedTrainerResponse = new AssignedTrainerResponse();
        TraineeAssignedTrainersUpdateResponse expected = new TraineeAssignedTrainersUpdateResponse();
        expected.setTrainers(List.of(assignedTrainerResponse));

        when(traineeService.updateTrainersList(any(TrainerAssignmentUpdateDTO.class)))
                .thenReturn(List.of(trainerInfoDTO));
        when(trainerRestMapper.toRest(trainerInfoDTO)).thenReturn(assignedTrainerResponse);

        TraineeAssignedTrainersUpdateResponse actual = facade.updateTraineeTrainersList(request, USERNAME);

        assertEquals(expected.getTrainers(), actual.getTrainers());
        verify(traineeService).updateTrainersList(TrainerAssignmentUpdateDTO.builder()
                .traineeUsername(USERNAME)
                .trainerUsernames(List.of("trainer1", "trainer2"))
                .build());
    }

    @Test
    void createTrainer_shouldSaveAndReturnResponseDTO() {
        TrainerCreateRequest request = mock(TrainerCreateRequest.class);

        TrainerRequestDTO dto = mock(TrainerRequestDTO.class);
        TrainerResponseDTO responseDTO = mock(TrainerResponseDTO.class);
        TrainerCreateResponse expectedResponse = mock(TrainerCreateResponse.class);

        when(trainerRestMapper.toDto(request)).thenReturn(dto);
        when(trainerService.createTrainer(dto)).thenReturn(responseDTO);
        when(trainerRestMapper.toRest(responseDTO)).thenReturn(expectedResponse);

        TrainerCreateResponse actual = facade.createTrainer(request);

        assertEquals(expectedResponse, actual);
    }

    @Test
    void getTrainerByUsername_shouldReturnMappedResponse() {
        TrainerInfoDTO trainerInfoDTO = mock(TrainerInfoDTO.class);
        TrainerGetResponse expectedResponse = mock(TrainerGetResponse.class);

        when(trainerService.getTrainerByUsername(USERNAME)).thenReturn(trainerInfoDTO);
        when(trainerRestMapper.toRestGetResponse(trainerInfoDTO)).thenReturn(expectedResponse);

        TrainerGetResponse actual = facade.getTrainerByUsername(USERNAME);

        assertEquals(expectedResponse, actual);
    }

    @Test
    void getTrainersNotAssignedToTrainee_shouldReturnMappedList() {
        TrainerInfoDTO infoDTO = TrainerInfoDTO.builder().build();
        AssignedTrainerResponse restResponse = new AssignedTrainerResponse();

        when(trainerService.getNotAssignedToTrainee(USERNAME)).thenReturn(List.of(infoDTO));
        when(trainerRestMapper.toRest(infoDTO)).thenReturn(restResponse);

        List<AssignedTrainerResponse> actual = facade.getTrainersNotAssignedToTrainee(USERNAME);

        assertEquals(1, actual.size());
        assertEquals(restResponse, actual.getFirst());
    }

    @Test
    void login_shouldDelegateToUserProfileService() {
        LoginRequest request = new LoginRequest(USERNAME, PASSWORD);

        facade.login(request);

        verify(userProfileService).login(request);
    }

    @Test
    void changePassword_shouldDelegateToUserProfileService() {
        LoginChangeRequest request = new LoginChangeRequest(USERNAME, OLD_PASSWORD, NEW_PASSWORD);

        facade.changePassword(request, USERNAME);

        verify(userProfileService).changePassword(PasswordChangeRequest.builder()
                .username(USERNAME)
                .oldPassword(OLD_PASSWORD)
                .newPassword(NEW_PASSWORD)
                .build());
    }

    @Test
    void changePassword_shouldChangePaswordAfterBuilding() {
        LoginChangeRequest request = new LoginChangeRequest(USERNAME, OLD_PASSWORD, NEW_PASSWORD);
        InOrder inOrder = inOrder(userProfileService);

        facade.changePassword(request, USERNAME);

        inOrder.verify(userProfileService).changePassword(any(PasswordChangeRequest.class));
    }

    private Trainee buildTrainee() {
        return Trainee.builder()
                .dateOfBirth(LocalDate.of(1980, 1, 1))
                .address("123 Oak St")
                .user(User.builder()
                        .firstName(FIRST_NAME).lastName(LAST_NAME)
                        .username(USERNAME).password(PASSWORD).isActive(true)
                        .build())
                .build();
    }

    private TraineeRequestDTO buildTraineeRequestDTO() {
        return TraineeRequestDTO.builder().firstName(FIRST_NAME).lastName(LAST_NAME).build();
    }

    private TraineeResponseDTO buildTraineeResponseDTO() {
        return TraineeResponseDTO.builder()
                .userId(VALID_ID).firstName(FIRST_NAME).lastName(LAST_NAME)
                .username(USERNAME).isActive(true)
                .build();
    }

    private Trainer buildTrainer() {
        return Trainer.builder()
                .specialization(TrainingType.builder().trainingTypeName(TRAINING_TYPE_NAME).build())
                .user(User.builder()
                        .firstName(FIRST_NAME).lastName(LAST_NAME)
                        .username(USERNAME).password(PASSWORD).isActive(true)
                        .build())
                .build();
    }

    private TrainerRequestDTO buildTrainerRequestDTO() {
        return TrainerRequestDTO.builder().firstName(FIRST_NAME).lastName(LAST_NAME).build();
    }

    private TrainerResponseDTO buildTrainerResponseDTO() {
        return TrainerResponseDTO.builder()
                .userId(TRAINER_ID).firstName(FIRST_NAME).lastName(LAST_NAME)
                .username(USERNAME).isActive(true)
                .build();
    }

    private Training buildTraining() {
        return Training.builder()
                .id(VALID_ID).trainingName(TRAINING_NAME)
                .trainingType(TrainingType.builder().trainingTypeName(TRAINING_TYPE_NAME).build())
                .trainingDate(LocalDate.of(2024, 1, 15)).trainingDuration(60)
                .trainee(Trainee.builder().id(VALID_ID).build())
                .trainer(Trainer.builder().id(VALID_ID).build())
                .build();
    }

    private TrainingRequestDTO buildTrainingRequestDTO() {
        return TrainingRequestDTO.builder()
                .traineeId(VALID_ID).trainerId(VALID_ID).trainingName(TRAINING_NAME)
                .trainingTypeName(TRAINING_TYPE_NAME).trainingDate(LocalDate.of(2024, 1, 15))
                .trainingDuration(60)
                .build();
    }

    private TrainingResponseDTO buildTrainingResponseDTO() {
        return TrainingResponseDTO.builder()
                .id(VALID_ID).traineeId(VALID_ID).trainerId(VALID_ID).trainingName(TRAINING_NAME)
                .trainingTypeName(TRAINING_TYPE_NAME).trainingDate(LocalDate.of(2024, 1, 15))
                .trainingDuration(60)
                .build();
    }
}