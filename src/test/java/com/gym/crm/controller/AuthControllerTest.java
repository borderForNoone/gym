package com.gym.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.crm.exception.ApiError;
import com.gym.crm.exception.ApiExceptionHandler;
import com.gym.crm.exception.BadCredentialsException;
import com.gym.crm.exception.EntityNotFoundException;
import com.gym.crm.exception.UserAuthenticationException;
import com.gym.crm.exception.UserAuthorizationException;
import com.gym.crm.facade.GymFacade;
import com.gym.crm.security.CustomUserDetailsService;
import com.gym.crm.security.JwtService;
import com.gym.crm.security.TokenBlacklistService;
import com.gym.crm.utils.JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.gym.crm.rest.LoginChangeRequest;
import org.gym.crm.rest.LoginRequest;
import org.gym.crm.rest.LoginResponse;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class AuthControllerTest {
    private static final String USERNAME = "Simone.Radcliffe";
    private static final String PASSWORD = "password";
    private static final String NEW_PASSWORD = "newPassword";
    private static final String BASE_URL = "/api/v1/auth";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private GymFacade facade;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;
    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void logout_shouldReturnOk_whenLogoutIsSuccessful() throws Exception {
        mockMvc.perform(post(BASE_URL + "/logout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void logout_shouldReturnClientError_whenLogoutFails() throws Exception {
        doThrow(new UserAuthenticationException("Missing or malformed Authorization header")).when(facade).logout(any(HttpServletRequest.class));

        mockMvc.perform(post(BASE_URL + "/logout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_shouldReturnOk() throws Exception {
        String requestBody = JsonUtil.readJson("json/auth/login_request.json");
        String expectedResponse = JsonUtil.readJson("json/auth/login_response.json");
        LoginResponse response = mapper.readValue(expectedResponse, LoginResponse.class);

        when(facade.login(any(LoginRequest.class))).thenReturn(response);

        String actualResponse = mockMvc.perform(post(BASE_URL + "/login").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JSONAssert.assertEquals(expectedResponse, actualResponse, true);
        verify(facade).login(any(LoginRequest.class));
    }

    @Test
    void changePassword_shouldReturnOk() throws Exception {
        String requestBody = JsonUtil.readJson("json/auth/change_password_request.json");

        ResultActions result = mockMvc.perform(put(BASE_URL + "/password").contentType(MediaType.APPLICATION_JSON).content(requestBody));

        result.andExpect(status().isOk());
        verify(facade).changePassword(any(LoginChangeRequest.class));
    }

    @Test
    void login_shouldReturnUnauthorized_whenBadCredentials() throws Exception {
        doThrow(new BadCredentialsException("Invalid credentials for user"))
                .when(facade).login(any(LoginRequest.class));

        ResultActions result = mockMvc.perform(post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(buildLoginRequest())));

        result.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value(ApiError.AUTHENTICATION_ERROR.getCode()))
                .andExpect(jsonPath("$.errorMessage").value("Authentication fails: Invalid credentials for user"));
        verify(facade).login(any(LoginRequest.class));
    }

    @Test
    void login_shouldReturnNotFound_whenUserNotFound() throws Exception {
        doThrow(new EntityNotFoundException("User not found")).when(facade).login(any(LoginRequest.class));

        ResultActions result = mockMvc.perform(post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(buildLoginRequest())));

        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(ApiError.NOT_FOUND_ERROR.getCode()))
                .andExpect(jsonPath("$.errorMessage").value("Requested data was not found: User not found"));
        verify(facade).login(any(LoginRequest.class));
    }

    @Test
    void changePassword_shouldReturnUnauthorized_whenNoUserAuthenticated() throws Exception {
        doThrow(new UserAuthenticationException("No user authenticated")).when(facade).changePassword(any(LoginChangeRequest.class));

        ResultActions result = mockMvc.perform(put(BASE_URL + "/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(buildLoginChangeRequest())));

        result.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value(ApiError.AUTHENTICATION_ERROR.getCode()))
                .andExpect(jsonPath("$.errorMessage").value("Authentication fails: No user authenticated"));
        verify(facade).changePassword(any(LoginChangeRequest.class));
    }

    @Test
    void changePassword_shouldReturnForbidden_whenUserNotAuthorized() throws Exception {
        doThrow(new UserAuthorizationException(
                "Authenticated user with username: other does not match with requested user with username: " + USERNAME))
                .when(facade).changePassword(any(LoginChangeRequest.class));

        ResultActions result = mockMvc.perform(put(BASE_URL + "/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(buildLoginChangeRequest())));

        result.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value(ApiError.AUTHORIZATION_ERROR.getCode()))
                .andExpect(jsonPath("$.errorMessage").value(
                        "User is not authorized for request operation: Authenticated user with username: other does not match with requested user with username: " + USERNAME));
        verify(facade).changePassword(any(LoginChangeRequest.class));
    }

    @Test
    void changePassword_shouldReturnNotFound_whenUserNotFound() throws Exception {
        doThrow(new EntityNotFoundException("User not found")).when(facade).changePassword(any(LoginChangeRequest.class));

        ResultActions result = mockMvc.perform(put(BASE_URL + "/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(buildLoginChangeRequest())));

        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(ApiError.NOT_FOUND_ERROR.getCode()))
                .andExpect(jsonPath("$.errorMessage").value("Requested data was not found: User not found"));
        verify(facade).changePassword(any(LoginChangeRequest.class));
    }

    private LoginRequest buildLoginRequest() {
        return new LoginRequest(USERNAME, PASSWORD);
    }

    private LoginChangeRequest buildLoginChangeRequest() {
        return new LoginChangeRequest(USERNAME, PASSWORD, NEW_PASSWORD);
    }
}