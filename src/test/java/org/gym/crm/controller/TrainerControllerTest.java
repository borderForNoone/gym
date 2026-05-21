package org.gym.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.gym.crm.facade.GymFacade;
import org.gym.crm.rest.ActivationStatusRequest;
import org.gym.crm.rest.GetTrainerTrainingResponse;
import org.gym.crm.rest.TrainerCreateRequest;
import org.gym.crm.rest.TrainerCreateResponse;
import org.gym.crm.rest.TrainerGetResponse;
import org.gym.crm.rest.TrainerUpdateRequest;
import org.gym.crm.rest.TrainerUpdateResponse;
import org.gym.crm.search.filter.TrainerTrainingFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerController unit tests")
class TrainerControllerTest {
    private static final String BASE_PATH = "/api/v1/trainers";

    @Mock
    private GymFacade facade;
    @InjectMocks
    private TrainerController trainerController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(trainerController)
                .addPlaceholderValue("app.api.base-path", "/api/v1")
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Test
    @DisplayName("POST /register – returns 200 with created trainer credentials")
    void register_validRequest_returns200() throws Exception {
        String requestJson = """
                {
                  "firstName": "John",
                  "lastName": "Doe",
                  "specialization": "Yoga"
                }
                """;

        TrainerCreateResponse response = new TrainerCreateResponse()
                .username("john.doe")
                .password("secret");

        when(facade.createTrainer(any(TrainerCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_PATH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john.doe"))
                .andExpect(jsonPath("$.password").value("secret"));

        verify(facade).createTrainer(any(TrainerCreateRequest.class));
    }

    @Test
    @DisplayName("POST /register – returns 400 when body is missing required fields")
    void register_invalidRequest_returns400() throws Exception {
        mockMvc.perform(post(BASE_PATH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /{username} – returns 200 with trainer profile")
    void getTrainerProfile_existingUsername_returns200() throws Exception {
        TrainerGetResponse response = new TrainerGetResponse()
                .firstName("John")
                .lastName("Doe")
                .specialization("Yoga")
                .isActive(true);

        when(facade.getTrainerByUsername("john.doe")).thenReturn(response);

        mockMvc.perform(get(BASE_PATH + "/john.doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.specialization").value("Yoga"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @DisplayName("PUT /{username} – returns 200 with updated trainer data")
    void updateTrainerProfile_validRequest_returns200() throws Exception {
        String requestJson = """
                {
                  "firstName": "Jane",
                  "lastName": "Doe",
                  "isActive": true
                }
                """;

        TrainerUpdateResponse response = new TrainerUpdateResponse()
                .firstName("Jane")
                .lastName("Doe")
                .isActive(true);

        when(facade.updateTrainer(any(TrainerUpdateRequest.class), eq("john.doe"))).thenReturn(response);

        mockMvc.perform(put(BASE_PATH + "/john.doe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(facade).updateTrainer(any(TrainerUpdateRequest.class), eq("john.doe"));
    }

    @Test
    @DisplayName("PUT /{username} – returns 400 when body fails validation")
    void updateTrainerProfile_invalidRequest_returns400() throws Exception {
        mockMvc.perform(put(BASE_PATH + "/john.doe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /{username}/activation – returns 200 and delegates to facade")
    void toggleActive_validRequest_returns200() throws Exception {
        String requestJson = """
                {
                  "isActive": false
                }
                """;

        mockMvc.perform(patch(BASE_PATH + "/john.doe/activation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        verify(facade).toggleActiveStatus(any(ActivationStatusRequest.class), eq("john.doe"));
    }

    @Test
    @DisplayName("PATCH /{username}/activation – returns 400 when body is empty")
    void toggleActive_invalidRequest_returns400() throws Exception {
        mockMvc.perform(patch(BASE_PATH + "/john.doe/activation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /{username}/trainings – returns 200 with list of trainings")
    void getTrainerTrainings_noFilters_returns200WithList() throws Exception {
        GetTrainerTrainingResponse t1 = new GetTrainerTrainingResponse()
                .trainingName("Morning Yoga");

        when(facade.getTrainerTrainingsByFilter(any(TrainerTrainingFilter.class), eq("john.doe")))
                .thenReturn(List.of(t1));

        mockMvc.perform(get(BASE_PATH + "/john.doe/trainings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingName").value("Morning Yoga"));
    }

    @Test
    @DisplayName("GET /{username}/trainings – passes date and name filters to filter object")
    void getTrainerTrainings_withFilters_buildsFilterCorrectly() throws Exception {
        when(facade.getTrainerTrainingsByFilter(any(TrainerTrainingFilter.class), eq("john.doe")))
                .thenReturn(List.of());

        mockMvc.perform(get(BASE_PATH + "/john.doe/trainings")
                        .param("fromDate", "2024-01-01")
                        .param("toDate", "2024-06-30")
                        .param("traineeName", "Alice"))
                .andExpect(status().isOk());

        ArgumentCaptor<TrainerTrainingFilter> captor = ArgumentCaptor.forClass(TrainerTrainingFilter.class);
        verify(facade).getTrainerTrainingsByFilter(captor.capture(), eq("john.doe"));

        TrainerTrainingFilter captured = captor.getValue();
        assertThat(captured.getUsername()).isEqualTo("john.doe");
        assertThat(captured.getFromDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(captured.getToDate()).isEqualTo(LocalDate.of(2024, 6, 30));
        assertThat(captured.getJoinFullName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("GET /{username}/trainings – returns empty array when facade returns empty list")
    void getTrainerTrainings_emptyResult_returnsEmptyArray() throws Exception {
        when(facade.getTrainerTrainingsByFilter(any(TrainerTrainingFilter.class), eq("john.doe")))
                .thenReturn(List.of());

        mockMvc.perform(get(BASE_PATH + "/john.doe/trainings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}