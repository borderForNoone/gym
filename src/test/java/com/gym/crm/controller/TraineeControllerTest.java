package com.gym.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.crm.exception.ApiError;
import com.gym.crm.exception.ApiExceptionHandler;
import com.gym.crm.exception.EntityNotFoundException;
import com.gym.crm.exception.UserAuthenticationException;
import com.gym.crm.exception.ValidationFailedException;
import com.gym.crm.facade.GymFacade;
import com.gym.crm.security.CustomUserDetailsService;
import com.gym.crm.security.JwtService;
import com.gym.crm.security.TokenBlacklistService;
import com.gym.crm.utils.JsonUtil;
import org.gym.crm.rest.GetTraineeTrainingResponse;
import org.gym.crm.rest.TraineeCreateRequest;
import org.gym.crm.rest.TraineeCreateResponse;
import org.gym.crm.rest.TraineeGetResponse;
import org.gym.crm.rest.TraineeUpdateRequest;
import org.gym.crm.rest.TraineeUpdateResponse;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TraineeController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
@TestPropertySource(properties = "app.api.base-path=/api/v1")
class TraineeControllerTest {
    private static final String BASE_URL = "/api/v1/trainees";
    private static final String USERNAME = "tom.tomas";

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
    void register_shouldReturnOk() throws Exception {
        TraineeCreateRequest request = new TraineeCreateRequest();
        request.setFirstName("Tom");
        request.setLastName("Tomas");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));
        request.setAddress("Kyiv");

        TraineeCreateResponse response = new TraineeCreateResponse(USERNAME, "password");

        when(facade.createTrainee(any())).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(USERNAME));
    }

    @Test
    void getProfile_shouldReturnOk() throws Exception {
        TraineeGetResponse response = new TraineeGetResponse();

        when(facade.getTraineeByUsername(USERNAME)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/" + USERNAME)).andExpect(status().isOk());
    }

    @Test
    void delete_shouldReturnOk() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + USERNAME)).andExpect(status().isOk());

        verify(facade).deleteTraineeByUsername(USERNAME);
    }

    @Test
    void getProfile_shouldReturnNotFound() throws Exception {
        when(facade.getTraineeByUsername(USERNAME)).thenThrow(new EntityNotFoundException("User not found"));

        mockMvc.perform(get(BASE_URL + "/" + USERNAME)).andExpect(status().isNotFound()).andExpect(jsonPath("$.errorCode")
                .value(ApiError.NOT_FOUND_ERROR.getCode()));
    }

    @Test
    void register_shouldReturnValidationError() throws Exception {
        TraineeCreateRequest request = new TraineeCreateRequest();

        when(facade.createTrainee(any())).thenThrow(new ValidationFailedException("Invalid data"));

        mockMvc.perform(post(BASE_URL + "/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value(ApiError.VALIDATION_ERROR.getCode()));
    }

    @Test
    void update_shouldReturnUnauthorized() throws Exception {
        TraineeUpdateRequest request = new TraineeUpdateRequest();

        when(facade.updateTrainee(any(), eq(USERNAME))).thenThrow(new UserAuthenticationException("No auth"));

        mockMvc.perform(put(BASE_URL + "/" + USERNAME).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.errorCode").value(ApiError.AUTHENTICATION_ERROR.getCode()));
    }

    @Test
    void getTrainings_shouldWork() throws Exception {
        when(facade.getTraineeTrainingsByFilter(any())).thenReturn(List.of(new GetTraineeTrainingResponse()));

        mockMvc.perform(get(BASE_URL + "/" + USERNAME + "/trainings")).andExpect(status().isOk());

        verify(facade).getTraineeTrainingsByFilter(any());
    }

    @Test
    void shouldRegisterTrainee() throws Exception {
        String request = JsonUtil.readJson("json/trainee/register-request.json");
        String response = JsonUtil.readJson("json/trainee/register-response.json");
        TraineeCreateResponse facadeResponse = new ObjectMapper().readValue(response, TraineeCreateResponse.class);

        when(facade.createTrainee(any())).thenReturn(facadeResponse);

        var result = mockMvc.perform(post(BASE_URL + "/register").contentType(MediaType.APPLICATION_JSON).content(request));
        result.andExpect(status().isOk()).andExpect(content().json(response));
    }

    @Test
    void shouldUpdateTrainee() throws Exception {
        String request = JsonUtil.readJson("json/trainee/update-request.json");
        String expectedResponse = JsonUtil.readJson("json/trainee/update-response.json");
        TraineeUpdateResponse facadeResponse = objectMapper.readValue(expectedResponse, TraineeUpdateResponse.class);

        when(facade.updateTrainee(any(), eq(USERNAME))).thenReturn(facadeResponse);

        MvcResult result = mockMvc.perform(put(BASE_URL + "/" + USERNAME).contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isOk())
                .andReturn();
        String actualResponse = result.getResponse().getContentAsString();
        JSONAssert.assertEquals(expectedResponse, actualResponse, true);
        verify(facade).updateTrainee(any(), eq(USERNAME));
    }
}