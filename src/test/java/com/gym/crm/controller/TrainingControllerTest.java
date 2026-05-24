package com.gym.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gym.crm.dto.TrainingRequestDTO;
import com.gym.crm.exception.ApiError;
import com.gym.crm.exception.ApiExceptionHandler;
import com.gym.crm.exception.ValidationFailedException;
import com.gym.crm.facade.GymFacade;
import org.gym.crm.rest.ErrorResponse;
import org.gym.crm.rest.TrainingCreateRequest;
import org.gym.crm.rest.TrainingTypeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TrainingControllerTest {
    private static final String BASE_URL = "/api/v1";

    @Mock
    private GymFacade facade;
    @InjectMocks
    private TrainingController controller;

    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new ApiExceptionHandler()).addPlaceholderValue("app.api.base-path", "/api/v1")
                .build();
    }

    @Test
    void addTraining_shouldReturnOkAndDelegateToFacade() {
        TrainingCreateRequest request = new TrainingCreateRequest();

        ResponseEntity<Void> response = controller.addTraining(request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNull();
        verify(facade).createTraining(request);
    }

    @Test
    void addTraining_shouldReturnBadRequest_whenRequiredFieldsMissing() throws Exception {
        TrainingCreateRequest request = buildValidRequest();
        request.setTraineeUsername(null);

        doThrow(new ValidationFailedException("traineeId must not be null, trainerId must not be null, trainingTypeName must not be blank"))
                .when(facade).createTraining(any(TrainingCreateRequest.class));

        String content = mockMvc.perform(post(BASE_URL + "/trainings").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse errorResponse = mapper.readValue(content, ErrorResponse.class);
        assertThat(errorResponse.getErrorCode()).isEqualTo(ApiError.VALIDATION_ERROR.getCode());
        assertThat(errorResponse.getErrorMessage()).contains("traineeId must not be null").contains("trainerId must not be null").contains("trainingTypeName must not be blank");
        verify(facade).createTraining(any(TrainingCreateRequest.class));
    }

    @Test
    void addTraining_shouldReturnBadRequest_whenDurationNegative() throws Exception {
        TrainingCreateRequest request = buildValidRequest();
        request.setTrainingDuration(-1);

        doThrow(new ValidationFailedException("trainingDuration must be greater than or equal to 1")).when(facade).createTraining(any(TrainingCreateRequest.class));

        String content = mockMvc.perform(post(BASE_URL + "/trainings").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse errorResponse = mapper.readValue(content, ErrorResponse.class);
        assertThat(errorResponse.getErrorCode()).isEqualTo(ApiError.VALIDATION_ERROR.getCode());
        assertThat(errorResponse.getErrorMessage()).contains("trainingDuration").contains("must be greater than or equal to 1");
        verify(facade).createTraining(any(TrainingCreateRequest.class));
    }

    @Test
    void getTrainingTypes_shouldReturnOkWithTypes() {
        List<TrainingTypeResponse> types = List.of(new TrainingTypeResponse(), new TrainingTypeResponse());
        when(facade.getTrainingTypes()).thenReturn(types);

        ResponseEntity<List<TrainingTypeResponse>> response = controller.getTrainingTypes();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(types);
        verify(facade).getTrainingTypes();
    }

    @Test
    void getTrainingTypes_shouldReturnEmptyListWhenNoneExist() {
        when(facade.getTrainingTypes()).thenReturn(List.of());

        ResponseEntity<List<TrainingTypeResponse>> response = controller.getTrainingTypes();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEmpty();
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