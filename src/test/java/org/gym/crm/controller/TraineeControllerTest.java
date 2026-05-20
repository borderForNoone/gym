package org.gym.crm.controller;

import org.gym.crm.dto.TraineeRequestDTO;
import org.gym.crm.dto.TraineeResponseDTO;
import org.gym.crm.facade.GymFacade;
import org.gym.crm.rest.ActivationStatusRequest;
import org.gym.crm.rest.AssignedTrainerResponse;
import org.gym.crm.rest.TraineeAssignedTrainersUpdateRequest;
import org.gym.crm.rest.TraineeAssignedTrainersUpdateResponse;
import org.gym.crm.rest.TraineeGetResponse;
import org.gym.crm.rest.TraineeUpdateRequest;
import org.gym.crm.rest.TraineeUpdateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeControllerTest {

    private static final String USERNAME = "john.doe";

    @Mock
    private GymFacade facade;

    @InjectMocks
    private TraineeController controller;

    private TraineeRequestDTO traineeRequestDTO;
    private TraineeResponseDTO traineeResponseDTO;
    private TraineeGetResponse traineeGetResponse;
    private TraineeUpdateRequest traineeUpdateRequest;
    private TraineeUpdateResponse traineeUpdateResponse;
    private ActivationStatusRequest activationStatusRequest;
    private TraineeAssignedTrainersUpdateRequest trainersUpdateRequest;
    private TraineeAssignedTrainersUpdateResponse trainersUpdateResponse;

    @BeforeEach
    void setUp() {
        traineeRequestDTO = TraineeRequestDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("Kyiv")
                .build();

        traineeResponseDTO = TraineeResponseDTO.builder()
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .username(USERNAME)
                .password("password")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("Kyiv")
                .isActive(true)
                .build();

        traineeGetResponse = new TraineeGetResponse();

        traineeUpdateRequest = new TraineeUpdateRequest();
        traineeUpdateResponse = new TraineeUpdateResponse();

        activationStatusRequest = new ActivationStatusRequest();

        trainersUpdateRequest = new TraineeAssignedTrainersUpdateRequest();
        trainersUpdateResponse = new TraineeAssignedTrainersUpdateResponse();
    }

    @Test
    void register_shouldReturnOkResponse() {
        when(facade.createTrainee(traineeRequestDTO))
                .thenReturn(traineeResponseDTO);

        ResponseEntity<TraineeResponseDTO> response =
                controller.register(traineeRequestDTO);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(traineeResponseDTO);

        verify(facade).createTrainee(traineeRequestDTO);
    }

    @Test
    void getTraineeProfile_shouldReturnOkResponse() {
        when(facade.getTraineeByUsername(USERNAME))
                .thenReturn(traineeGetResponse);

        ResponseEntity<TraineeGetResponse> response =
                controller.getTraineeProfile(USERNAME);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(traineeGetResponse);

        verify(facade).getTraineeByUsername(USERNAME);
    }

    @Test
    void updateTraineeProfile_shouldReturnOkResponse() {
        when(facade.updateTrainee(traineeUpdateRequest, USERNAME))
                .thenReturn(traineeUpdateResponse);

        ResponseEntity<TraineeUpdateResponse> response =
                controller.updateTraineeProfile(USERNAME, traineeUpdateRequest);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(traineeUpdateResponse);

        verify(facade).updateTrainee(traineeUpdateRequest, USERNAME);
    }

    @Test
    void deleteTrainee_shouldReturnOkResponse() {
        ResponseEntity<Void> response =
                controller.deleteTrainee(USERNAME);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNull();

        verify(facade).deleteTraineeByUsername(USERNAME);
    }

    @Test
    void toggleActive_shouldReturnOkResponse() {
        ResponseEntity<Void> response =
                controller.toggleActive(USERNAME, activationStatusRequest);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNull();

        verify(facade).toggleActiveStatus(activationStatusRequest, USERNAME);
    }

    @Test
    void updateTraineeTrainers_shouldReturnOkResponse() {
        when(facade.updateTraineeTrainersList(trainersUpdateRequest, USERNAME))
                .thenReturn(trainersUpdateResponse);

        ResponseEntity<TraineeAssignedTrainersUpdateResponse> response =
                controller.updateTraineeTrainers(USERNAME, trainersUpdateRequest);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(trainersUpdateResponse);

        verify(facade).updateTraineeTrainersList(trainersUpdateRequest, USERNAME);
    }

    @Test
    void getAvailableTrainers_shouldReturnOkResponse() {
        List<AssignedTrainerResponse> trainers = List.of(
                new AssignedTrainerResponse()
        );

        when(facade.getTrainersNotAssignedToTrainee(USERNAME))
                .thenReturn(trainers);

        ResponseEntity<List<AssignedTrainerResponse>> response =
                controller.getAvailableTrainers(USERNAME);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(trainers);

        verify(facade).getTrainersNotAssignedToTrainee(USERNAME);
    }
}