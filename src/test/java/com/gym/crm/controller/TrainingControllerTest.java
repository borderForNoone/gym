package com.gym.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.crm.exception.ApiError;
import com.gym.crm.exception.ApiExceptionHandler;
import com.gym.crm.exception.ValidationFailedException;
import com.gym.crm.facade.GymFacade;
import com.gym.crm.security.CustomUserDetailsService;
import com.gym.crm.security.JwtService;
import com.gym.crm.security.TokenBlacklistService;
import org.gym.crm.rest.TrainingCreateRequest;
import org.gym.crm.rest.TrainingTypeResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainingController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class TrainingControllerTest {
    private static final String BASE_URL = "/api/v1";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GymFacade facade;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private CustomUserDetailsService userDetailsService;
    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void addTraining_shouldReturnOkAndDelegateToFacade() throws Exception {
        TrainingCreateRequest request = buildValidRequest();

        mockMvc.perform(post(BASE_URL + "/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(facade).createTraining(any(TrainingCreateRequest.class));
    }

    @Test
    void addTraining_shouldReturnBadRequest_whenRequiredFieldsMissing() throws Exception {
        TrainingCreateRequest request = buildValidRequest();
        request.setTraineeUsername(null);
        doThrow(new ValidationFailedException("traineeId must not be null, trainerId must not be null, trainingTypeName must not be blank"))
                .when(facade).createTraining(any(TrainingCreateRequest.class));

        ResultActions result = mockMvc.perform(post(BASE_URL + "/trainings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ApiError.VALIDATION_ERROR.getCode()))
                .andExpect(jsonPath("$.errorMessage", containsString("traineeId must not be null")));
        verify(facade).createTraining(any(TrainingCreateRequest.class));
    }

    @Test
    void addTraining_shouldReturnBadRequest_whenDurationNegative() throws Exception {
        TrainingCreateRequest request = buildValidRequest();
        request.setTrainingDuration(-1);
        doThrow(new ValidationFailedException("trainingDuration must be greater than or equal to 1"))
                .when(facade).createTraining(any(TrainingCreateRequest.class));

        ResultActions result = mockMvc.perform(post(BASE_URL + "/trainings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ApiError.VALIDATION_ERROR.getCode()))
                .andExpect(jsonPath("$.errorMessage", containsString("trainingDuration")));
        verify(facade).createTraining(any(TrainingCreateRequest.class));
    }

    @Test
    void getTrainingTypes_shouldReturnOkWithTypes() throws Exception {
        List<TrainingTypeResponse> types = List.of(new TrainingTypeResponse(), new TrainingTypeResponse());
        when(facade.getTrainingTypes()).thenReturn(types);

        mockMvc.perform(get(BASE_URL + "/trainings/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(facade).getTrainingTypes();
    }

    @Test
    void getTrainingTypes_shouldReturnEmptyListWhenNoneExist() throws Exception {
        when(facade.getTrainingTypes()).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL + "/trainings/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(facade).getTrainingTypes();
    }

    private TrainingCreateRequest buildValidRequest() {
        TrainingCreateRequest request = new TrainingCreateRequest();
        request.setTrainingName("Morning Yoga");
        request.setTrainingDate(LocalDate.of(2025, 1, 1));
        request.setTrainingDuration(60);
        request.setTraineeUsername("trainee.user");
        request.setTrainerUsername("trainer.user");
        return request;
    }
}