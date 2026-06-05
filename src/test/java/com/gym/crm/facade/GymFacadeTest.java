package com.gym.crm.facade;

import com.gym.crm.facade.dto.AuthResponseDTO;
import com.gym.crm.facade.dto.CreatedTrainee;
import com.gym.crm.facade.dto.CreatedTrainer;
import com.gym.crm.facade.dto.PasswordChangeRequest;
import com.gym.crm.facade.dto.ToggleActiveRequestDTO;
import com.gym.crm.facade.dto.TraineeInfoDTO;
import com.gym.crm.facade.dto.TraineeRequestDTO;
import com.gym.crm.facade.dto.TraineeResponseDTO;
import com.gym.crm.facade.dto.TraineeUpdateDTO;
import com.gym.crm.facade.dto.TrainerAssignmentUpdateDTO;
import com.gym.crm.facade.dto.TrainerInfoDTO;
import com.gym.crm.facade.dto.TrainerRequestDTO;
import com.gym.crm.facade.dto.TrainerResponseDTO;
import com.gym.crm.facade.dto.TrainerUpdateDTO;
import com.gym.crm.facade.dto.TrainingRequestDTO;
import com.gym.crm.facade.dto.TrainingResponseDTO;
import com.gym.crm.facade.dto.TrainingTypeDTO;
import com.gym.crm.mapper.TraineeMapper;
import com.gym.crm.mapper.TraineeRestMapper;
import com.gym.crm.mapper.TrainerMapper;
import com.gym.crm.mapper.TrainerRestMapper;
import com.gym.crm.mapper.TrainingMapper;
import com.gym.crm.mapper.TrainingRestMapper;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.User;
import com.gym.crm.search.filter.TraineeTrainingFilter;
import com.gym.crm.search.filter.TrainerTrainingFilter;
import com.gym.crm.service.TraineeService;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.TrainingService;
import com.gym.crm.service.UserProfileService;
import com.gym.crm.service.common.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import org.gym.crm.rest.ActivationStatusRequest;
import org.gym.crm.rest.AssignedTrainerResponse;
import org.gym.crm.rest.GetTraineeTrainingResponse;
import org.gym.crm.rest.GetTrainerTrainingResponse;
import org.gym.crm.rest.LoginChangeRequest;
import org.gym.crm.rest.LoginRequest;
import org.gym.crm.rest.LoginResponse;
import org.gym.crm.rest.TraineeAssignedTrainersUpdateRequest;
import org.gym.crm.rest.TraineeAssignedTrainersUpdateResponse;
import org.gym.crm.rest.TraineeCreateRequest;
import org.gym.crm.rest.TraineeCreateResponse;
import org.gym.crm.rest.TraineeGetResponse;
import org.gym.crm.rest.TraineeUpdateRequest;
import org.gym.crm.rest.TraineeUpdateResponse;
import org.gym.crm.rest.TrainerCreateRequest;
import org.gym.crm.rest.TrainerCreateResponse;
import org.gym.crm.rest.TrainerGetResponse;
import org.gym.crm.rest.TrainerUpdateRequest;
import org.gym.crm.rest.TrainerUpdateResponse;
import org.gym.crm.rest.TrainingCreateRequest;
import org.gym.crm.rest.TrainingTypeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.naming.AuthenticationException;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    @Mock
    private TrainingRestMapper trainingRestMapper;
    @Mock
    private AuthenticationService authenticationService;

    private GymFacade facade;
    private Trainee trainee;
    private Trainer trainer;
    private Training training;
    private TraineeResponseDTO traineeResponseDTO;
    private TrainerResponseDTO trainerResponseDTO;
    private TrainingRequestDTO trainingRequestDTO;
    private TrainingResponseDTO trainingResponseDTO;

    @BeforeEach
    void setUp() {
        facade = new GymFacade(traineeService, trainerService, trainingService, userProfileService, traineeRestMapper, trainerRestMapper, trainingRestMapper, authenticationService);
        facade.setTraineeMapper(traineeMapper);
        facade.setTrainerMapper(trainerMapper);
        facade.setTrainingMapper(trainingMapper);

        trainee = buildTrainee();
        traineeResponseDTO = buildTraineeResponseDTO();
        trainer = buildTrainer();
        trainerResponseDTO = buildTrainerResponseDTO();
        training = buildTraining();
        trainingRequestDTO = buildTrainingRequestDTO();
        trainingResponseDTO = buildTrainingResponseDTO();
    }

    @Test
    void logout_shouldCallClearContext() {
        HttpServletRequest request = new MockHttpServletRequest();

        facade.logout(request);

        verify(authenticationService).logout(request);
    }

    @Test
    void createTrainee_shouldSaveAndReturnResponseDTO() {
        TraineeCreateRequest request = new TraineeCreateRequest();
        request.setFirstName(FIRST_NAME);
        request.setLastName(LAST_NAME);

        TraineeRequestDTO dto = buildTraineeRequestDTO();

        Trainee saved = trainee.toBuilder().id(VALID_ID).user(trainee.getUser().toBuilder().id(VALID_ID).username(USERNAME).password(PASSWORD).isActive(true).build())
                .build();
        CreatedTrainee createdTrainee = new CreatedTrainee(saved, PASSWORD);
        TraineeCreateResponse expectedResponse = new TraineeCreateResponse(USERNAME, PASSWORD);

        when(traineeRestMapper.toDto(request)).thenReturn(dto);
        when(traineeMapper.toEntity(dto)).thenReturn(trainee);
        when(traineeService.create(trainee)).thenReturn(createdTrainee);
        when(traineeMapper.toDto(saved)).thenReturn(traineeResponseDTO);
        when(traineeRestMapper.toRest(any(TraineeResponseDTO.class))).thenReturn(expectedResponse);

        TraineeCreateResponse actual = facade.createTrainee(request);

        assertThat(actual.getUsername()).isEqualTo(USERNAME);
        assertThat(actual.getPassword()).isEqualTo(PASSWORD);
        verify(traineeRestMapper).toDto(request);
        verify(traineeMapper).toEntity(dto);
        verify(traineeService).create(trainee);
        verify(traineeMapper).toDto(saved);
        verify(traineeRestMapper).toRest(any(TraineeResponseDTO.class));
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

        verify(userProfileService).toggleActive(ToggleActiveRequestDTO.builder().username(USERNAME).isActive(true).build());
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
        verify(trainingMapper, never()).toDto((Training) any());
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
        verify(trainingMapper, never()).toDto((Training) any());
    }

    @Test
    void createTraining_shouldSaveAndReturnResponseDTO() {
        TrainingCreateRequest request = new TrainingCreateRequest();
        TrainingRequestDTO dto = trainingRequestDTO;

        when(trainingRestMapper.toDto(request)).thenReturn(dto);
        when(trainingMapper.toEntity(dto)).thenReturn(training);
        when(trainingService.create(training)).thenReturn(training);

        facade.createTraining(request);

        verify(trainingRestMapper).toDto(request);
        verify(trainingMapper).toEntity(dto);
        verify(trainingService).create(training);
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

        when(traineeService.updateTrainersList(any(TrainerAssignmentUpdateDTO.class))).thenReturn(List.of(trainerInfoDTO));
        when(trainerRestMapper.toRest(trainerInfoDTO)).thenReturn(assignedTrainerResponse);

        TraineeAssignedTrainersUpdateResponse actual = facade.updateTraineeTrainersList(request, USERNAME);

        assertEquals(expected.getTrainers(), actual.getTrainers());
        verify(traineeService).updateTrainersList(TrainerAssignmentUpdateDTO.builder().traineeUsername(USERNAME).trainerUsernames(List.of("trainer1", "trainer2")).build());
    }

    @Test
    void createTrainer_shouldSaveAndReturnResponseDTO() {
        TrainerCreateRequest request = mock(TrainerCreateRequest.class);
        TrainerRequestDTO dto = mock(TrainerRequestDTO.class);
        Trainer savedTrainer = mock(Trainer.class);
        CreatedTrainer createdTrainer = new CreatedTrainer(savedTrainer, "rawPassword");
        TrainerResponseDTO responseDTO = TrainerResponseDTO.builder().username(USERNAME).password("rawPassword").isActive(true).build();
        TrainerCreateResponse expectedResponse = mock(TrainerCreateResponse.class);

        when(trainerRestMapper.toDto(request)).thenReturn(dto);
        when(trainerService.createTrainer(dto)).thenReturn(createdTrainer);
        when(trainerMapper.toDto(savedTrainer)).thenReturn(responseDTO);
        when(trainerRestMapper.toRest(any(TrainerResponseDTO.class))).thenReturn(expectedResponse);

        TrainerCreateResponse actual = facade.createTrainer(request);

        assertEquals(expectedResponse, actual);
        verify(trainerRestMapper).toDto(request);
        verify(trainerService).createTrainer(dto);
        verify(trainerMapper).toDto(savedTrainer);
        verify(trainerRestMapper).toRest(any(TrainerResponseDTO.class));
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
    void login_shouldReturnLoginResponse_withTokenAndUsername() {
        LoginRequest request = new LoginRequest();
        request.setUsername(USERNAME);
        request.setPassword(PASSWORD);
        AuthResponseDTO authResponse = AuthResponseDTO.builder()
                .username(USERNAME)
                .token("jwt-token")
                .build();

        when(userProfileService.authenticate(USERNAME, PASSWORD)).thenReturn(authResponse);

        LoginResponse result = facade.login(request);

        assertThat(result.getUsername()).isEqualTo(USERNAME);
        assertThat(result.getToken()).isEqualTo("jwt-token");
        verify(userProfileService).authenticate(USERNAME, PASSWORD);
    }

    @Test
    void changePassword_shouldDelegateToUserProfileService() {
        LoginChangeRequest request = new LoginChangeRequest(USERNAME, OLD_PASSWORD, NEW_PASSWORD);

        facade.changePassword(request);

        verify(userProfileService).changePassword(PasswordChangeRequest.builder().username(USERNAME).oldPassword(OLD_PASSWORD).newPassword(NEW_PASSWORD).build());
    }

    @Test
    void changePassword_shouldChangePaswordAfterBuilding() {
        LoginChangeRequest request = new LoginChangeRequest(USERNAME, OLD_PASSWORD, NEW_PASSWORD);
        InOrder inOrder = inOrder(userProfileService);

        facade.changePassword(request);

        inOrder.verify(userProfileService).changePassword(any(PasswordChangeRequest.class));
    }

    @Test
    void getTraineeTrainingsByFilter_shouldReturnMappedResponses() {
        TraineeTrainingFilter filter = TraineeTrainingFilter.builder().build();
        TrainingResponseDTO dto1 = TrainingResponseDTO.builder().id(1L).build();
        TrainingResponseDTO dto2 = TrainingResponseDTO.builder().id(2L).build();
        GetTraineeTrainingResponse response1 = new GetTraineeTrainingResponse();
        GetTraineeTrainingResponse response2 = new GetTraineeTrainingResponse();

        when(trainingService.getTraineeTrainings(filter)).thenReturn(List.of(dto1, dto2));
        when(trainingRestMapper.toRestTraineeResponse(dto1)).thenReturn(response1);
        when(trainingRestMapper.toRestTraineeResponse(dto2)).thenReturn(response2);

        List<GetTraineeTrainingResponse> result = facade.getTraineeTrainingsByFilter(filter);

        assertThat(result).containsExactly(response1, response2);
        verify(trainingRestMapper).toRestTraineeResponse(dto1);
        verify(trainingRestMapper).toRestTraineeResponse(dto2);
    }

    @Test
    void getTraineeTrainingsByFilter_shouldReturnEmptyListWhenNoTrainings() {
        TraineeTrainingFilter filter = TraineeTrainingFilter.builder().build();
        when(trainingService.getTraineeTrainings(filter)).thenReturn(List.of());

        List<GetTraineeTrainingResponse> result = facade.getTraineeTrainingsByFilter(filter);

        assertThat(result).isEmpty();
        verifyNoInteractions(trainingRestMapper);
    }

    @Test
    void getTrainerTrainingsByFilter_shouldReturnMappedResponses() {
        TrainerTrainingFilter filter = TrainerTrainingFilter.builder().build();
        TrainingResponseDTO dto1 = TrainingResponseDTO.builder().id(1L).build();
        TrainingResponseDTO dto2 = TrainingResponseDTO.builder().id(2L).build();
        GetTrainerTrainingResponse response1 = new GetTrainerTrainingResponse();
        GetTrainerTrainingResponse response2 = new GetTrainerTrainingResponse();

        when(trainingService.getTrainerTrainings(filter)).thenReturn(List.of(dto1, dto2));
        when(trainingRestMapper.toRestTrainerResponse(dto1)).thenReturn(response1);
        when(trainingRestMapper.toRestTrainerResponse(dto2)).thenReturn(response2);

        List<GetTrainerTrainingResponse> result = facade.getTrainerTrainingsByFilter(filter);

        assertThat(result).containsExactly(response1, response2);
        verify(trainingService).getTrainerTrainings(filter);
        verify(trainingRestMapper).toRestTrainerResponse(dto1);
        verify(trainingRestMapper).toRestTrainerResponse(dto2);
    }

    @Test
    void getTrainerTrainingsByFilter_shouldReturnEmptyListWhenNoTrainings() {
        TrainerTrainingFilter filter = TrainerTrainingFilter.builder().build();
        when(trainingService.getTrainerTrainings(filter)).thenReturn(List.of());

        List<GetTrainerTrainingResponse> result = facade.getTrainerTrainingsByFilter(filter);

        assertThat(result).isEmpty();
        verifyNoInteractions(trainingRestMapper);
    }

    @Test
    void changeTraineePassword_shouldDelegateToTraineeService() throws AuthenticationException {
        facade.changeTraineePassword("tom.tomas", "old123", "new456");

        verify(traineeService).changePassword("tom.tomas", "old123", "new456");
    }

    @Test
    void changeTraineePassword_shouldPropagateAuthenticationException() throws AuthenticationException {
        doThrow(new AuthenticationException("Invalid credentials")).when(traineeService).changePassword("tom.tomas", "wrong", "new456");

        assertThatThrownBy(() -> facade.changeTraineePassword("tom.tomas", "wrong", "new456")).isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void changeTrainerPassword_shouldDelegateToTrainerService() throws AuthenticationException {
        facade.changeTrainerPassword("julia.tomas", "old123", "new456");

        verify(trainerService).changePassword("julia.tomas", "old123", "new456");
    }

    @Test
    void changeTrainerPassword_shouldPropagateAuthenticationException() throws AuthenticationException {
        doThrow(new AuthenticationException("Invalid credentials")).when(trainerService).changePassword("julia.tomas", "wrong", "new456");

        assertThatThrownBy(() -> facade.changeTrainerPassword("julia.tomas", "wrong", "new456")).isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void updateTrainer_shouldMapRequestAndReturnResponse() {
        TrainerUpdateRequest request = new TrainerUpdateRequest();
        TrainerUpdateDTO dto = TrainerUpdateDTO.builder().build();
        TrainerResponseDTO responseDTO = TrainerResponseDTO.builder().build();
        TrainerUpdateResponse updateResponse = new TrainerUpdateResponse();

        when(trainerRestMapper.toDto("julia.tomas", request)).thenReturn(dto);
        when(trainerService.updateTrainer(dto)).thenReturn(responseDTO);
        when(trainerRestMapper.toRestUpdateResponse(responseDTO)).thenReturn(updateResponse);

        TrainerUpdateResponse result = facade.updateTrainer(request, "julia.tomas");

        assertThat(result).isEqualTo(updateResponse);
        var inOrder = inOrder(trainerRestMapper, trainerService);
        inOrder.verify(trainerRestMapper).toDto("julia.tomas", request);
        inOrder.verify(trainerService).updateTrainer(dto);
        inOrder.verify(trainerRestMapper).toRestUpdateResponse(responseDTO);
    }

    @Test
    void getTrainingTypes_shouldReturnMappedResponses() {
        TrainingTypeDTO typeDto1 = TrainingTypeDTO.builder().id(1L).trainingTypeName("Yoga").build();
        TrainingTypeDTO typeDto2 = TrainingTypeDTO.builder().id(2L).trainingTypeName("Boxing").build();
        TrainingTypeResponse response1 = new TrainingTypeResponse();
        TrainingTypeResponse response2 = new TrainingTypeResponse();

        when(trainingService.getAllTrainingTypes()).thenReturn(List.of(typeDto1, typeDto2));
        when(trainingRestMapper.toRest(typeDto1)).thenReturn(response1);
        when(trainingRestMapper.toRest(typeDto2)).thenReturn(response2);

        List<TrainingTypeResponse> result = facade.getTrainingTypes();

        assertThat(result).containsExactly(response1, response2);
        verify(trainingService).getAllTrainingTypes();
    }

    @Test
    void getTrainingTypes_shouldReturnEmptyListWhenNoneExist() {
        when(trainingService.getAllTrainingTypes()).thenReturn(List.of());

        List<TrainingTypeResponse> result = facade.getTrainingTypes();

        assertThat(result).isEmpty();
        verifyNoInteractions(trainingRestMapper);
    }

    private Trainee buildTrainee() {
        return Trainee.builder().dateOfBirth(LocalDate.of(1980, 1, 1)).address("123 Oak St")
                .user(User.builder()
                        .firstName(FIRST_NAME)
                        .lastName(LAST_NAME)
                        .username(USERNAME)
                        .password(PASSWORD)
                        .isActive(true).build())
                .build();
    }

    private TraineeRequestDTO buildTraineeRequestDTO() {
        return TraineeRequestDTO.builder().firstName(FIRST_NAME).lastName(LAST_NAME).build();
    }

    private TraineeResponseDTO buildTraineeResponseDTO() {
        return TraineeResponseDTO.builder().userId(VALID_ID).firstName(FIRST_NAME).lastName(LAST_NAME).username(USERNAME).isActive(true).build();
    }

    private Trainer buildTrainer() {
        return Trainer.builder().specialization(TrainingType.builder().trainingTypeName(TRAINING_TYPE_NAME).build()).user(User.builder()
                        .firstName(FIRST_NAME)
                        .lastName(LAST_NAME)
                        .username(USERNAME)
                        .password(PASSWORD)
                        .isActive(true).build())
                .build();
    }

    private TrainerResponseDTO buildTrainerResponseDTO() {
        return TrainerResponseDTO.builder().userId(TRAINER_ID).firstName(FIRST_NAME).lastName(LAST_NAME).username(USERNAME).isActive(true).build();
    }

    private Training buildTraining() {
        return Training.builder().id(VALID_ID).trainingName(TRAINING_NAME).trainingType(TrainingType.builder().trainingTypeName(TRAINING_TYPE_NAME).build())
                .trainingDate(LocalDate.of(2024, 1, 15)).trainingDuration(60).trainee(Trainee.builder().id(VALID_ID).build())
                .trainer(Trainer.builder().id(VALID_ID).build()).build();
    }

    private TrainingRequestDTO buildTrainingRequestDTO() {
        return new TrainingRequestDTO(VALID_ID, VALID_ID, TRAINING_NAME, TRAINING_TYPE_NAME, LocalDate.of(2024, 1, 15), 60);
    }

    private TrainingResponseDTO buildTrainingResponseDTO() {
        return TrainingResponseDTO.builder().id(VALID_ID).traineeId(VALID_ID).trainerId(VALID_ID).trainingName(TRAINING_NAME)
                .trainingTypeName(TRAINING_TYPE_NAME).trainingDate(LocalDate.of(2024, 1, 15)).trainingDuration(60).build();
    }
}