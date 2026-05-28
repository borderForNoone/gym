package com.gym.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.crm.exception.ApiError;
import com.gym.crm.exception.EntityNotFoundException;
import com.gym.crm.exception.UserAuthenticationException;
import com.gym.crm.exception.ValidationFailedException;
import com.gym.crm.facade.GymFacade;
import com.gym.crm.search.filter.TrainerTrainingFilter;
import jakarta.persistence.PersistenceException;
import org.gym.crm.rest.ActivationStatusRequest;
import org.gym.crm.rest.ErrorResponse;
import org.gym.crm.rest.GetTrainerTrainingResponse;
import org.gym.crm.rest.TrainerCreateRequest;
import org.gym.crm.rest.TrainerCreateResponse;
import org.gym.crm.rest.TrainerGetResponse;
import org.gym.crm.rest.TrainerUpdateRequest;
import org.gym.crm.rest.TrainerUpdateResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static com.gym.crm.util.TestConstants.USERNAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainerController.class)
@DisplayName("TrainerController unit tests")
class TrainerControllerTest {
    private static final String BASE_PATH = "/api/v1/trainers";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GymFacade facade;

    @Test
    void register_shouldReturnNotValid_whenFirstNameMissing() throws Exception {
        TrainerCreateRequest request = new TrainerCreateRequest();
        request.setLastName("Tomas");
        request.setSpecialization("Yoga");

        doThrow(new ValidationFailedException("firstName must not be null")).when(facade).createTrainer(any(TrainerCreateRequest.class));

        String content = mockMvc.perform(post(BASE_PATH + "/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse errorResponse = objectMapper.readValue(content, ErrorResponse.class);
        assertThat(errorResponse.getErrorCode()).isEqualTo(ApiError.VALIDATION_ERROR.getCode());
        assertThat(errorResponse.getErrorMessage()).isEqualTo("Validation error: firstName must not be null");
        verify(facade).createTrainer(any(TrainerCreateRequest.class));
    }

    @Test
    void getTrainerProfile_shouldReturnNotFound_whenTrainerNotFound() throws Exception {
        doThrow(new EntityNotFoundException("User not found")).when(facade).getTrainerByUsername(USERNAME);

        String content = mockMvc.perform(get(BASE_PATH + "/" + USERNAME)).andExpect(status().isNotFound()).andReturn().getResponse().getContentAsString();

        ErrorResponse errorResponse = objectMapper.readValue(content, ErrorResponse.class);
        assertThat(errorResponse.getErrorCode()).isEqualTo(ApiError.NOT_FOUND_ERROR.getCode());
        assertThat(errorResponse.getErrorMessage()).isEqualTo("Requested data was not found: User not found");
        verify(facade).getTrainerByUsername(USERNAME);
    }

    @Test
    void updateTrainerProfile_shouldReturnNotValid_whenFirstNameMissing() throws Exception {
        TrainerUpdateRequest request = new TrainerUpdateRequest();
        request.setLastName("Tomas");
        request.setIsActive(true);

        doThrow(new ValidationFailedException("firstName must not be null")).when(facade).updateTrainer(any(TrainerUpdateRequest.class), eq(USERNAME));

        String content = mockMvc.perform(put(BASE_PATH + "/" + USERNAME).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse errorResponse = objectMapper.readValue(content, ErrorResponse.class);
        assertThat(errorResponse.getErrorCode()).isEqualTo(ApiError.VALIDATION_ERROR.getCode());
        assertThat(errorResponse.getErrorMessage()).isEqualTo("Validation error: firstName must not be null");
        verify(facade).updateTrainer(any(TrainerUpdateRequest.class), eq(USERNAME));
    }

    @Test
    void updateTrainerProfile_shouldReturnNotFound_whenTrainerNotFound() throws Exception {
        TrainerUpdateRequest request = new TrainerUpdateRequest();
        request.setFirstName("Tom");
        request.setLastName("Tomas");
        request.setIsActive(true);

        doThrow(new EntityNotFoundException("User not found")).when(facade).updateTrainer(any(TrainerUpdateRequest.class), eq(USERNAME));

        String content = mockMvc.perform(put(BASE_PATH + "/" + USERNAME).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse errorResponse = objectMapper.readValue(content, ErrorResponse.class);
        assertThat(errorResponse.getErrorCode()).isEqualTo(ApiError.NOT_FOUND_ERROR.getCode());
        assertThat(errorResponse.getErrorMessage()).isEqualTo("Requested data was not found: User not found");
        verify(facade).updateTrainer(any(TrainerUpdateRequest.class), eq(USERNAME));
    }

    @Test
    void updateTrainerProfile_shouldReturnUnauthorized_whenNoUserAuthenticated() throws Exception {
        TrainerUpdateRequest request = new TrainerUpdateRequest();
        request.setFirstName("Tom");
        request.setLastName("Tomas");
        request.setIsActive(true);

        doThrow(new UserAuthenticationException("No user authenticated")).when(facade).updateTrainer(any(TrainerUpdateRequest.class), eq(USERNAME));

        String content = mockMvc.perform(put(BASE_PATH + "/" + USERNAME).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();

        ErrorResponse errorResponse = objectMapper.readValue(content, ErrorResponse.class);
        assertThat(errorResponse.getErrorCode()).isEqualTo(ApiError.AUTHENTICATION_ERROR.getCode());
        assertThat(errorResponse.getErrorMessage()).isEqualTo("Authentication fails: No user authenticated");
        verify(facade).updateTrainer(any(TrainerUpdateRequest.class), eq(USERNAME));
    }

    @Test
    void updateTrainerProfile_shouldReturnDBFailure_whenPersistenceException() throws Exception {
        TrainerUpdateRequest request = new TrainerUpdateRequest();
        request.setFirstName("Tom");
        request.setLastName("Tomas");
        request.setIsActive(true);

        doThrow(new PersistenceException()).when(facade).updateTrainer(any(TrainerUpdateRequest.class), eq(USERNAME));

        String content = mockMvc.perform(put(BASE_PATH + "/" + USERNAME).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError()).andReturn().getResponse().getContentAsString();

        ErrorResponse errorResponse = objectMapper.readValue(content, ErrorResponse.class);
        assertThat(errorResponse.getErrorCode()).isEqualTo(ApiError.DATABASE_ERROR.getCode());
        assertThat(errorResponse.getErrorMessage()).isEqualTo("Unexpected database access failure");
        verify(facade).updateTrainer(any(TrainerUpdateRequest.class), eq(USERNAME));
    }

    @Test
    void updateTrainerProfile_shouldReturnUnhandledException_whenUnexpectedError() throws Exception {
        TrainerUpdateRequest request = new TrainerUpdateRequest();
        request.setFirstName("Tom");
        request.setLastName("Tomas");
        request.setIsActive(true);

        doThrow(new RuntimeException()).when(facade).updateTrainer(any(TrainerUpdateRequest.class), eq(USERNAME));

        String content = mockMvc.perform(put(BASE_PATH + "/" + USERNAME).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError()).andReturn().getResponse().getContentAsString();

        ErrorResponse errorResponse = objectMapper.readValue(content, ErrorResponse.class);
        assertThat(errorResponse.getErrorCode()).isEqualTo(ApiError.SERVICE_ERROR.getCode());
        assertThat(errorResponse.getErrorMessage()).isEqualTo("Internal processing error");
        verify(facade).updateTrainer(any(TrainerUpdateRequest.class), eq(USERNAME));
    }

    @Test
    @DisplayName("POST /register – returns 200 with created trainer credentials")
    void register_validRequest_returns200() throws Exception {
        String requestJson = """
                {
                  "firstName": "Tom",
                  "lastName": "Tomas",
                  "specialization": "Yoga"
                }
                """;

        TrainerCreateResponse response = new TrainerCreateResponse().username("tom.tomas").password("secret");

        when(facade.createTrainer(any(TrainerCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_PATH + "/register").contentType(MediaType.APPLICATION_JSON).content(requestJson)).andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("tom.tomas")).andExpect(jsonPath("$.password").value("secret"));

        verify(facade).createTrainer(any(TrainerCreateRequest.class));
    }

    @Test
    @DisplayName("POST /register – returns 400 when body is missing required fields")
    void register_invalidRequest_returns400() throws Exception {
        doThrow(new ValidationFailedException("firstName must not be null")).when(facade).createTrainer(any(TrainerCreateRequest.class));

        mockMvc.perform(post(BASE_PATH + "/register").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /{username} – returns 200 with trainer profile")
    void getTrainerProfile_existingUsername_returns200() throws Exception {
        TrainerGetResponse response = new TrainerGetResponse().firstName("Tom").lastName("Tomas").specialization("Yoga").isActive(true);

        when(facade.getTrainerByUsername("tom.tomas")).thenReturn(response);

        mockMvc.perform(get(BASE_PATH + "/tom.tomas")).andExpect(status().isOk()).andExpect(jsonPath("$.firstName").value("Tom"))
                .andExpect(jsonPath("$.lastName").value("Tomas")).andExpect(jsonPath("$.specialization").value("Yoga"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @DisplayName("PUT /{username} – returns 200 with updated trainer data")
    void updateTrainerProfile_validRequest_returns200() throws Exception {
        String requestJson = """
                {
                  "firstName": "Tom",
                  "lastName": "Tomas",
                  "isActive": true
                }
                """;

        TrainerUpdateResponse response = new TrainerUpdateResponse().firstName("Julia").lastName("Tomas").isActive(true);

        when(facade.updateTrainer(any(TrainerUpdateRequest.class), eq("tom.tomas"))).thenReturn(response);

        mockMvc.perform(put(BASE_PATH + "/tom.tomas").contentType(MediaType.APPLICATION_JSON).content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Julia"))
                .andExpect(jsonPath("$.lastName").value("Tomas"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(facade).updateTrainer(any(TrainerUpdateRequest.class), eq("tom.tomas"));
    }

    @Test
    @DisplayName("PUT /{username} – returns 400 when body fails validation")
    void updateTrainerProfile_invalidRequest_returns400() throws Exception {
        doThrow(new ValidationFailedException("firstName must not be null")).when(facade).updateTrainer(any(TrainerUpdateRequest.class), eq("tom.tomas"));

        mockMvc.perform(put(BASE_PATH + "/tom.tomas").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /{username}/activation – returns 200 and delegates to facade")
    void toggleActive_validRequest_returns200() throws Exception {
        String requestJson = """
                {
                  "isActive": false
                }
                """;

        mockMvc.perform(patch(BASE_PATH + "/tom.tomas/activation").contentType(MediaType.APPLICATION_JSON).content(requestJson)).andExpect(status().isOk());

        verify(facade).toggleActiveStatus(any(ActivationStatusRequest.class), eq("tom.tomas"));
    }

    @Test
    @DisplayName("PATCH /{username}/activation – returns 400 when body is empty")
    void toggleActive_invalidRequest_returns400() throws Exception {
        doThrow(new ValidationFailedException("isActive must not be null")).when(facade).toggleActiveStatus(any(ActivationStatusRequest.class), eq("tom.tomas"));

        mockMvc.perform(patch(BASE_PATH + "/tom.tomas/activation").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /{username}/trainings – returns 200 with list of trainings")
    void getTrainerTrainings_noFilters_returns200WithList() throws Exception {
        GetTrainerTrainingResponse t1 = new GetTrainerTrainingResponse().trainingName("Morning Yoga");

        when(facade.getTrainerTrainingsByFilter(any(TrainerTrainingFilter.class))).thenReturn(List.of(t1));

        mockMvc.perform(get(BASE_PATH + "/tom.tomas/trainings")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingName").value("Morning Yoga"));
    }

    @Test
    @DisplayName("GET /{username}/trainings – passes date and name filters to filter object")
    void getTrainerTrainings_withFilters_buildsFilterCorrectly() throws Exception {
        when(facade.getTrainerTrainingsByFilter(any(TrainerTrainingFilter.class))).thenReturn(List.of());

        mockMvc.perform(get(BASE_PATH + "/tom.tomas/trainings")
                        .param("fromDate", "2024-01-01").param("toDate", "2024-06-30")
                        .param("traineeName", "Alice"))
                .andExpect(status().isOk());

        ArgumentCaptor<TrainerTrainingFilter> captor = ArgumentCaptor.forClass(TrainerTrainingFilter.class);
        verify(facade).getTrainerTrainingsByFilter(captor.capture());

        TrainerTrainingFilter captured = captor.getValue();
        assertThat(captured.getUsername()).isEqualTo("tom.tomas");
        assertThat(captured.getFromDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(captured.getToDate()).isEqualTo(LocalDate.of(2024, 6, 30));
        assertThat(captured.getJoinFullName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("GET /{username}/trainings – returns empty array when facade returns empty list")
    void getTrainerTrainings_emptyResult_returnsEmptyArray() throws Exception {
        when(facade.getTrainerTrainingsByFilter(any(TrainerTrainingFilter.class))).thenReturn(List.of());

        mockMvc.perform(get(BASE_PATH + "/tom.tomas/trainings")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }
}