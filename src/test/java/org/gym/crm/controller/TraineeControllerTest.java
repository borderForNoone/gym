package org.gym.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gym.crm.dto.TraineeRequestDTO;
import org.gym.crm.dto.TraineeResponseDTO;
import org.gym.crm.exception.ApiError;
import org.gym.crm.exception.ApiExceptionHandler;
import org.gym.crm.exception.EntityNotFoundException;
import org.gym.crm.exception.UserAuthenticationException;
import org.gym.crm.exception.ValidationFailedException;
import org.gym.crm.facade.GymFacade;
import org.gym.crm.rest.ActivationStatusRequest;
import org.gym.crm.rest.AssignedTrainerResponse;
import org.gym.crm.rest.ErrorResponse;
import org.gym.crm.rest.GetTraineeTrainingResponse;
import org.gym.crm.rest.TraineeAssignedTrainersUpdateRequest;
import org.gym.crm.rest.TraineeAssignedTrainersUpdateResponse;
import org.gym.crm.rest.TraineeGetResponse;
import org.gym.crm.rest.TraineeUpdateRequest;
import org.gym.crm.rest.TraineeUpdateResponse;
import org.gym.crm.search.filter.TraineeTrainingFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TraineeControllerTest {
    private static final String BASE_URL = "/api/v1/trainees";
    private static final String USERNAME = "tom.tomas";
    private static final String TRAINER_USERNAME = "julia.trainer";

    private MockMvc mockMvc;
    private ObjectMapper mapper;

    @Mock
    private GymFacade facade;

    private TraineeController controller;

    private TraineeRequestDTO traineeRequestDTO;
    private TraineeResponseDTO traineeResponseDTO;
    private TraineeGetResponse traineeGetResponse;
    private TraineeUpdateRequest traineeUpdateRequest;
    private TraineeUpdateResponse traineeUpdateResponse;

    @BeforeEach
    void setUp() {
        controller = new TraineeController(facade);
        mapper = new ObjectMapper().findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .addPlaceholderValue("app.api.base-path", "/api/v1")
                .build();
        traineeRequestDTO = TraineeRequestDTO.builder()
                .firstName("Tom")
                .lastName("Tomas")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("Kyiv")
                .build();
        traineeResponseDTO = TraineeResponseDTO.builder()
                .userId(1L)
                .firstName("Tom")
                .lastName("Tomas")
                .username(USERNAME)
                .password("password")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("Kyiv")
                .isActive(true)
                .build();
        traineeGetResponse = new TraineeGetResponse();
        traineeUpdateRequest = new TraineeUpdateRequest();
        traineeUpdateResponse = new TraineeUpdateResponse();
    }

    @Test
    void register_shouldReturnOkResponse() throws Exception {
        when(facade.createTrainee(any(TraineeRequestDTO.class)))
                .thenReturn(traineeResponseDTO);

        String content = mockMvc.perform(post(BASE_URL + "/register").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(traineeRequestDTO)))
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();

        TraineeResponseDTO body = mapper.readValue(content, TraineeResponseDTO.class);
        assertThat(body).isEqualTo(traineeResponseDTO);
        verify(facade).createTrainee(any(TraineeRequestDTO.class));
    }

    @Test
    void getTraineeProfile_shouldReturnOkResponse() throws Exception {
        when(facade.getTraineeByUsername(USERNAME)).thenReturn(traineeGetResponse);

        String content = mockMvc.perform(get(BASE_URL + "/" + USERNAME)).andExpect(status().is2xxSuccessful()).andReturn().getResponse().getContentAsString();

        TraineeGetResponse body = mapper.readValue(content, TraineeGetResponse.class);
        assertThat(body).isEqualTo(traineeGetResponse);
        verify(facade).getTraineeByUsername(USERNAME);
    }

    @Test
    void updateTraineeProfile_shouldReturnOkResponse() throws Exception {
        traineeUpdateRequest = new TraineeUpdateRequest();
        traineeUpdateRequest.setFirstName("Tom");
        traineeUpdateRequest.setLastName("Tomas");
        traineeUpdateRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));
        traineeUpdateRequest.setAddress("Kyiv");
        traineeUpdateRequest.setIsActive(true);

        when(facade.updateTrainee(any(TraineeUpdateRequest.class), eq(USERNAME))).thenReturn(traineeUpdateResponse);

        String content = mockMvc.perform(put(BASE_URL + "/" + USERNAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(traineeUpdateRequest)))
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();

        TraineeUpdateResponse body = mapper.readValue(content, TraineeUpdateResponse.class);
        assertThat(body).isEqualTo(traineeUpdateResponse);
        verify(facade).updateTrainee(any(TraineeUpdateRequest.class), eq(USERNAME));
    }

    @Test
    void deleteTrainee_shouldReturnOkResponse() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + USERNAME)).andExpect(status().is2xxSuccessful());

        verify(facade).deleteTraineeByUsername(USERNAME);
    }

    @Test
    void toggleActive_shouldReturnOkResponse() throws Exception {
        ActivationStatusRequest request = new ActivationStatusRequest();
        request.setIsActive(true);

        mockMvc.perform(patch(BASE_URL + "/" + USERNAME + "/activation").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(request)))
                .andExpect(status().is2xxSuccessful());

        verify(facade).toggleActiveStatus(any(ActivationStatusRequest.class), eq(USERNAME));
    }

    @Test
    void updateTraineeTrainers_shouldReturnOkResponse() throws Exception {
        TraineeAssignedTrainersUpdateRequest request = new TraineeAssignedTrainersUpdateRequest(List.of(TRAINER_USERNAME));
        TraineeAssignedTrainersUpdateResponse response = new TraineeAssignedTrainersUpdateResponse();

        when(facade.updateTraineeTrainersList(any(TraineeAssignedTrainersUpdateRequest.class), eq(USERNAME))).thenReturn(response);

        String content = mockMvc.perform(put(BASE_URL + "/" + USERNAME + "/trainers").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))).andExpect(status().is2xxSuccessful()).andReturn().getResponse().getContentAsString();

        TraineeAssignedTrainersUpdateResponse body = mapper.readValue(content, TraineeAssignedTrainersUpdateResponse.class);

        assertThat(body).isEqualTo(response);
        verify(facade).updateTraineeTrainersList(any(TraineeAssignedTrainersUpdateRequest.class), eq(USERNAME));
    }

    @Test
    void getAvailableTrainers_shouldReturnOkResponse() throws Exception {
        List<AssignedTrainerResponse> trainers = List.of(new AssignedTrainerResponse());
        when(facade.getTrainersNotAssignedToTrainee(USERNAME)).thenReturn(trainers);

        mockMvc.perform(get(BASE_URL + "/" + USERNAME + "/available-trainers")).andExpect(status().is2xxSuccessful());

        verify(facade).getTrainersNotAssignedToTrainee(USERNAME);
    }

    @Test
    void getTraineeTrainings_shouldReturnOkWithAllParams() throws Exception {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        List<GetTraineeTrainingResponse> trainings = List.of(new GetTraineeTrainingResponse());

        TraineeTrainingFilter filter = TraineeTrainingFilter.builder()
                .username(USERNAME)
                .fromDate(from)
                .toDate(to)
                .joinFullName("Julia Tomas")
                .trainingTypeName("Yoga")
                .build();

        when(facade.getTraineeTrainingsByFilter(filter)).thenReturn(trainings);

        mockMvc.perform(get(BASE_URL + "/" + USERNAME + "/trainings")
                        .param("fromDate", from.toString())
                        .param("toDate", to.toString())
                        .param("trainerName", "Julia Tomas")
                        .param("trainingType", "Yoga"))
                .andExpect(status().is2xxSuccessful());

        verify(facade).getTraineeTrainingsByFilter(filter);
    }

    @Test
    void getTraineeTrainings_shouldReturnOkWithNullOptionalParams() throws Exception {
        List<GetTraineeTrainingResponse> trainings = List.of(new GetTraineeTrainingResponse());

        TraineeTrainingFilter filter = TraineeTrainingFilter.builder().username(USERNAME).fromDate(null).toDate(null).joinFullName(null).trainingTypeName(null).build();

        when(facade.getTraineeTrainingsByFilter(filter)).thenReturn(trainings);

        mockMvc.perform(get(BASE_URL + "/" + USERNAME + "/trainings")).andExpect(status().is2xxSuccessful());

        verify(facade).getTraineeTrainingsByFilter(filter);
    }

    @Test
    void getTraineeTrainings_shouldReturnEmptyList() throws Exception {
        TraineeTrainingFilter filter = TraineeTrainingFilter.builder()
                .username(USERNAME)
                .fromDate(null).toDate(null)
                .joinFullName(null).trainingTypeName(null)
                .build();

        when(facade.getTraineeTrainingsByFilter(filter)).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL + "/" + USERNAME + "/trainings")).andExpect(status().is2xxSuccessful());
    }

    @Test
    void register_shouldReturnNotValid_whenDateOfBirthInTheFuture() throws Exception {
        TraineeRequestDTO request = TraineeRequestDTO.builder()
                .firstName("Tom")
                .lastName("Tomas")
                .dateOfBirth(LocalDate.of(2030, 1, 1))
                .address("Kyiv")
                .build();

        doThrow(new ValidationFailedException("Date of birth must be in the past")).when(facade).createTrainee(any(TraineeRequestDTO.class));

        String content = mockMvc.perform(post(BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorResponse errorResponse = mapper.readValue(content, ErrorResponse.class);
        assertThat(errorResponse.getErrorCode()).isEqualTo(ApiError.VALIDATION_ERROR.getCode());
        assertThat(errorResponse.getErrorMessage()).isEqualTo("Validation error: Date of birth must be in the past");

        verify(facade).createTrainee(any(TraineeRequestDTO.class));
    }

    @Test
    void getTraineeProfile_shouldReturnNotFound_whenTraineeNotFound() throws Exception {
        doThrow(new EntityNotFoundException("User not found")).when(facade).getTraineeByUsername(USERNAME);

        String content = mockMvc.perform(get(BASE_URL + "/" + USERNAME)).andExpect(status().isNotFound()).andReturn().getResponse().getContentAsString();

        ErrorResponse errorResponse = mapper.readValue(content, ErrorResponse.class);
        assertThat(errorResponse.getErrorCode()).isEqualTo(ApiError.NOT_FOUND_ERROR.getCode());
        assertThat(errorResponse.getErrorMessage()).isEqualTo("Requested data was not found: User not found");

        verify(facade).getTraineeByUsername(USERNAME);
    }

    @Test
    void updateTraineeProfile_shouldReturnNotValid_whenFirstNameMissing() throws Exception {
        TraineeUpdateRequest request = TestDataProvider.buildTraineeUpdateRequest();
        request.setFirstName(null);

        String content = mockMvc.perform(put(BASE_URL + "/" + USERNAME).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorResponse errorResponse = mapper.readValue(content, ErrorResponse.class);
        assertThat(errorResponse.getErrorCode()).isEqualTo(ApiError.VALIDATION_ERROR.getCode());
        assertThat(errorResponse.getErrorMessage()).isEqualTo("Validation error: firstName must not be null");

        verifyNoInteractions(facade);
    }

    @Test
    void updateTraineeProfile_shouldReturnNotFound_whenTraineeNotFound() throws Exception {
        TraineeUpdateRequest request = TestDataProvider.buildTraineeUpdateRequest();
        doThrow(new EntityNotFoundException("User not found")).when(facade).updateTrainee(any(TraineeUpdateRequest.class), eq(USERNAME));

        String content = mockMvc.perform(put(BASE_URL + "/" + USERNAME).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        ErrorResponse errorResponse = mapper.readValue(content, ErrorResponse.class);
        assertThat(errorResponse.getErrorCode()).isEqualTo(ApiError.NOT_FOUND_ERROR.getCode());
        assertThat(errorResponse.getErrorMessage()).isEqualTo("Requested data was not found: User not found");

        verify(facade).updateTrainee(any(TraineeUpdateRequest.class), eq(USERNAME));
    }

    @Test
    void updateTraineeProfile_shouldReturnUnauthorized_whenNoUserAuthenticated() throws Exception {
        TraineeUpdateRequest request = TestDataProvider.buildTraineeUpdateRequest();

        doThrow(new UserAuthenticationException("No user authenticated")).when(facade).updateTrainee(any(TraineeUpdateRequest.class), eq(USERNAME));

        String content = mockMvc.perform(put(BASE_URL + "/" + USERNAME).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        ErrorResponse errorResponse = mapper.readValue(content, ErrorResponse.class);
        assertThat(errorResponse.getErrorCode()).isEqualTo(ApiError.AUTHENTICATION_ERROR.getCode());
        assertThat(errorResponse.getErrorMessage()).isEqualTo("Authentication fails: No user authenticated");

        verify(facade).updateTrainee(any(TraineeUpdateRequest.class), eq(USERNAME));
    }

    @Test
    void deleteTrainee_shouldReturnNotFound_whenTraineeNotFound() throws Exception {
        doThrow(new EntityNotFoundException("User not found")).when(facade).deleteTraineeByUsername(USERNAME);

        String content = mockMvc.perform(delete(BASE_URL + "/" + USERNAME)).andExpect(status().isNotFound()).andReturn().getResponse().getContentAsString();

        ErrorResponse errorResponse = mapper.readValue(content, ErrorResponse.class);
        assertThat(errorResponse.getErrorCode()).isEqualTo(ApiError.NOT_FOUND_ERROR.getCode());
        assertThat(errorResponse.getErrorMessage()).isEqualTo("Requested data was not found: User not found");

        verify(facade).deleteTraineeByUsername(USERNAME);
    }

    @Test
    void updateTraineeTrainers_shouldReturnUnauthorized_whenNoUserAuthenticated() throws Exception {
        TraineeAssignedTrainersUpdateRequest request = new TraineeAssignedTrainersUpdateRequest(List.of(TRAINER_USERNAME));

        doThrow(new UserAuthenticationException("No user authenticated")).when(facade).updateTraineeTrainersList(any(TraineeAssignedTrainersUpdateRequest.class),
                eq(USERNAME));

        String content = mockMvc.perform(put(BASE_URL + "/" + USERNAME + "/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        ErrorResponse errorResponse = mapper.readValue(content, ErrorResponse.class);
        assertThat(errorResponse.getErrorCode()).isEqualTo(ApiError.AUTHENTICATION_ERROR.getCode());
        assertThat(errorResponse.getErrorMessage()).isEqualTo("Authentication fails: No user authenticated");

        verify(facade).updateTraineeTrainersList(any(TraineeAssignedTrainersUpdateRequest.class), eq(USERNAME));
    }

    private static class TestDataProvider {
        static TraineeRequestDTO buildTraineeCreateRequest() {
            return TraineeRequestDTO.builder()
                    .firstName("Tom")
                    .lastName("Tomas")
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .address("Kyiv")
                    .build();
        }

        static TraineeUpdateRequest buildTraineeUpdateRequest() {
            TraineeUpdateRequest request = new TraineeUpdateRequest();
            request.setFirstName("Tom");
            request.setLastName("Tomas");
            request.setDateOfBirth(LocalDate.of(1990, 1, 1));
            request.setAddress("Kyiv");
            request.setIsActive(true);
            return request;
        }
    }
}