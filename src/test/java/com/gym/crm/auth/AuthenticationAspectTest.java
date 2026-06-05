package com.gym.crm.auth;

import com.gym.crm.exception.UserAuthenticationException;
import com.gym.crm.exception.UserAuthorizationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthenticationAspectTest {
    private static final String USERNAME = "Simone.Radcliffe";

    private final AuthenticationAspect aspect = new AuthenticationAspect();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void checkAuthentication_shouldThrow_whenUsernameIsNull() {
        UserAuthenticationException exception = assertThrows(UserAuthenticationException.class, () -> aspect.checkAuthentication(null));

        assertThat(exception.getMessage()).isEqualTo("User is not authenticated: no request to check authentication");
    }

    @Test
    void checkAuthentication_shouldThrow_whenNoAuthenticationInContext() {
        UserAuthenticationException exception = assertThrows(UserAuthenticationException.class, () -> aspect.checkAuthentication(USERNAME));

        assertThat(exception.getMessage()).isEqualTo("No user authenticated");
    }

    @Test
    void checkAuthentication_shouldThrow_whenAuthenticationIsNotAuthenticated() {
        UsernamePasswordAuthenticationToken unauthenticated = new UsernamePasswordAuthenticationToken(USERNAME, null, null);
        unauthenticated.setAuthenticated(false);
        SecurityContextHolder.getContext().setAuthentication(unauthenticated);

        UserAuthenticationException exception = assertThrows(UserAuthenticationException.class, () -> aspect.checkAuthentication(USERNAME));

        assertThat(exception.getMessage()).isEqualTo("No user authenticated");
    }

    @Test
    void checkAuthentication_shouldThrow_whenUsernameDoesNotMatch() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("OtherUser", null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserAuthorizationException exception = assertThrows(UserAuthorizationException.class, () -> aspect.checkAuthentication(USERNAME));

        assertThat(exception.getMessage()).contains(String.format("Authenticated user with username: OtherUser does not match with requested user with username: %s", USERNAME));
    }

    @Test
    void checkAuthentication_shouldPass_whenUsernameMatches() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(USERNAME, null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        aspect.checkAuthentication(USERNAME);
    }
}