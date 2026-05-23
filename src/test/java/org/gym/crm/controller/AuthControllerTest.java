package org.gym.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gym.crm.exception.ApiError;
import org.gym.crm.exception.ApiExceptionHandler;
import org.gym.crm.exception.BadCredentialsException;
import org.gym.crm.exception.EntityNotFoundException;
import org.gym.crm.exception.UserAuthenticationException;
import org.gym.crm.exception.UserAuthorizationException;
import org.gym.crm.facade.GymFacade;
import org.gym.crm.rest.ErrorResponse;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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
    private final LoginRequest loginRequest = buildLoginRequest();
    private final LoginChangeRequest loginChangeRequest = buildLoginChangeRequest();

    private MockMvc mockMvc;

    @Mock
    private GymFacade facade;

    @BeforeEach
    void setUp() {
        AuthController controller = new AuthController(facade);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .addPlaceholderValue("app.api.base-path", "/api/v1")
                .build();
    }

    @Test
    void login_shouldReturnOk() throws Exception {
        mockMvc.perform(post(BASE_URL + "/login").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
        verify(facade).login(any(LoginRequest.class));
    }

    @Test
    void changePassword_shouldReturnOk() throws Exception {
        mockMvc.perform(put(BASE_URL + "/password").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
        verify(facade).changePassword(any(LoginChangeRequest.class), any(String.class));
    }

    @Test
    void login_shouldReturnBadCredentialsException_whenInvalidPassword() throws Exception {
        doThrow(new BadCredentialsException("Invalid credentials for user")).when(facade).login(any(LoginRequest.class));

        String content = mockMvc.perform(post(BASE_URL + "/login").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loginRequest))).andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();

        ErrorResponse errorResponse = mapper.readValue(content, ErrorResponse.class);

        assertThat(errorResponse.getErrorCode()).isEqualTo(ApiError.AUTHENTICATION_ERROR.getCode());
        assertThat(errorResponse.getErrorMessage()).isEqualTo("Authentication fails: Invalid credentials for user");
        verify(facade).login(any(LoginRequest.class));
    }

    @Test
    void login_shouldEntityNotFoundException_whenUserNotFound() throws Exception {
        doThrow(new EntityNotFoundException("User not found")).when(facade).login(any(LoginRequest.class));

        String content = mockMvc.perform(post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loginRequest)))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse errorResponse = mapper.readValue(content, ErrorResponse.class);

        assertThat(errorResponse.getErrorCode()).isEqualTo(ApiError.NOT_FOUND_ERROR.getCode());
        assertThat(errorResponse.getErrorMessage()).isEqualTo("Requested data was not found: User not found");
        verify(facade).login(any(LoginRequest.class));
    }

    @Test
    void changePassword_shouldReturnUnauthorized_whenNoUserAuthenticated() throws Exception {
        doThrow(new UserAuthenticationException("No user authenticated"))
                .when(facade).changePassword(any(LoginChangeRequest.class), eq(USERNAME));

        String content = mockMvc.perform(put(BASE_URL + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loginChangeRequest)))
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse errorResponse = mapper.readValue(content, ErrorResponse.class);

        assertThat(errorResponse.getErrorCode()).isEqualTo(ApiError.AUTHENTICATION_ERROR.getCode());
        assertThat(errorResponse.getErrorMessage()).isEqualTo("Authentication fails: No user authenticated");
        verify(facade).changePassword(any(LoginChangeRequest.class), eq(USERNAME));
    }

    @Test
    void changePassword_shouldReturnForbidden_whenUserNotAuthorized() throws Exception {
        doThrow(new UserAuthorizationException("Authenticated user with username: other does not match with requested user with username: " + USERNAME))
                .when(facade).changePassword(any(LoginChangeRequest.class), eq(USERNAME));

        String content = mockMvc.perform(put(BASE_URL + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loginChangeRequest)))
                .andExpect(status().isForbidden())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse errorResponse = mapper.readValue(content, ErrorResponse.class);

        assertThat(errorResponse.getErrorCode()).isEqualTo(ApiError.AUTHORIZATION_ERROR.getCode());
        assertThat(errorResponse.getErrorMessage()).isEqualTo("User is not authorized for request operation: Authenticated user with username: other does not match with requested user with username: " + USERNAME);
        verify(facade).changePassword(any(LoginChangeRequest.class), eq(USERNAME));
    }

    @Test
    void changePassword_shouldReturnNotFound_whenUserNotFound() throws Exception {
        doThrow(new EntityNotFoundException("User not found")).when(facade).changePassword(any(LoginChangeRequest.class), eq(USERNAME));

        String content = mockMvc.perform(put(BASE_URL + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loginChangeRequest)))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorResponse errorResponse = mapper.readValue(content, ErrorResponse.class);

        assertThat(errorResponse.getErrorCode()).isEqualTo(ApiError.NOT_FOUND_ERROR.getCode());
        assertThat(errorResponse.getErrorMessage()).isEqualTo("Requested data was not found: User not found");
        verify(facade).changePassword(any(LoginChangeRequest.class), eq(USERNAME));
    }

    private LoginRequest buildLoginRequest() {
        return new LoginRequest(USERNAME, PASSWORD);
    }

    private LoginChangeRequest buildLoginChangeRequest() {
        return new LoginChangeRequest(USERNAME, PASSWORD, NEW_PASSWORD);
    }
}