package com.gym.crm.auth;

import com.gym.crm.exception.UserAuthenticationException;
import com.gym.crm.exception.UserAuthorizationException;
import com.gym.crm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationAspectTest {
    @Mock
    private SessionContext sessionContext;

    @InjectMocks
    private AuthenticationAspect aspect;

    private User authenticatedUser;

    @BeforeEach
    void setUp() {
        authenticatedUser = User.builder()
                .username("Tom.Tomas")
                .build();
    }

    @Test
    void checkAuthentication_shouldPass_whenUserMatchesSession() {
        when(sessionContext.getAuthenticatedUser()).thenReturn(authenticatedUser);

        assertDoesNotThrow(() -> aspect.checkAuthentication("Tom.Tomas"));
    }

    @Test
    void checkAuthentication_shouldThrow_whenNoUserInSession() {
        when(sessionContext.getAuthenticatedUser()).thenReturn(null);

        UserAuthenticationException ex = assertThrows(UserAuthenticationException.class, () -> aspect.checkAuthentication("Tom.Tomas"));

        assertTrue(ex.getMessage().contains("No user authenticated"));
    }

    @Test
    void checkAuthentication_shouldThrow_whenUsernameArgumentIsNull() {
        when(sessionContext.getAuthenticatedUser()).thenReturn(authenticatedUser);

        UserAuthenticationException ex = assertThrows(UserAuthenticationException.class, () -> aspect.checkAuthentication(null));

        assertTrue(ex.getMessage().contains("no request to check authentication"));
    }

    @Test
    void checkAuthentication_shouldThrow_whenUsernameDoesNotMatchSession() {
        when(sessionContext.getAuthenticatedUser()).thenReturn(authenticatedUser);

        UserAuthorizationException ex = assertThrows(UserAuthorizationException.class, () -> aspect.checkAuthentication("Julia.Tomas"));

        assertTrue(ex.getMessage().contains("Tom.Tomas"));
        assertTrue(ex.getMessage().contains("Julia.Tomas"));
    }
}