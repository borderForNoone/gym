package org.gym.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gym.crm.facade.GymFacade;
import org.gym.crm.rest.LoginChangeRequest;
import org.gym.crm.rest.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    private static final String USERNAME = "Simone.Radcliffe";
    private static final String PASSWORD = "password";
    private static final String NEW_PASSWORD = "newPassword";
    private static final String BASE_URL = "/api/v1/auth";

    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Mock
    private GymFacade facade;

    @BeforeEach
    void setUp() {
        AuthController controller = new AuthController(facade);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addPlaceholderValue("app.api.base-path", "/api/v1")
                .build();
    }

    @Test
    void login_shouldReturnOk() throws Exception {
        LoginRequest loginRequest = buildLoginRequest();

        mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
        verify(facade).login(any(LoginRequest.class));
    }

    @Test
    void changePassword_shouldReturnOk() throws Exception {
        LoginChangeRequest loginRequest = buildLoginChangeRequest();

        mockMvc.perform(put(BASE_URL + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
        verify(facade).changePassword(any(LoginChangeRequest.class), any(String.class));
    }

    private LoginRequest buildLoginRequest() {
        return new LoginRequest(USERNAME, PASSWORD);
    }

    private LoginChangeRequest buildLoginChangeRequest() {
        return new LoginChangeRequest(USERNAME, PASSWORD, NEW_PASSWORD);
    }
}