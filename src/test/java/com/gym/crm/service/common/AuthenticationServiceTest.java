package com.gym.crm.service.common;

import com.gym.crm.exception.UserAuthenticationException;
import com.gym.crm.facade.dto.AuthRequestDTO;
import com.gym.crm.facade.dto.AuthResponseDTO;
import com.gym.crm.model.User;
import com.gym.crm.repository.UserRepository;
import com.gym.crm.security.BruteForceProtectionService;
import com.gym.crm.security.JwtService;
import com.gym.crm.security.TokenBlacklistService;
import com.gym.crm.service.UserProfileService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    private static final String USERNAME = "Simone.Radcliffe";
    private static final String PASSWORD = "password";
    private static final String ENCODED_PASSWORD = "encodedPassword";
    private static final String INVALID_PASSWORD = "invalidPassword";
    private static final String TOKEN = "token";
    private static final String INVALID_HEADER_ERROR = "Missing or malformed Authorization header";

    @Mock
    private UserRepository repository;
    @Mock
    private UserProfileService userProfileService;
    @Mock
    private JwtService jwtService;
    @Mock
    private TokenBlacklistService tokenBlacklistService;
    @Mock
    private BruteForceProtectionService bruteForceProtectionService;

    @InjectMocks
    private AuthenticationService service;

    @Test
    void authenticate_shouldReturnResponse_whenCredentialsAreValid() {
        doNothing().when(bruteForceProtectionService).checkIfLocked(USERNAME);
        when(repository.findByUsername(USERNAME)).thenReturn(Optional.of(buildUser()));
        when(userProfileService.checkPassword(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(jwtService.generateToken(USERNAME)).thenReturn(TOKEN);

        AuthResponseDTO actual = service.authenticate(buildAuthRequest());

        assertThat(actual.getUsername()).isEqualTo(USERNAME);
        assertThat(actual.getToken()).isEqualTo(TOKEN);
        verify(bruteForceProtectionService).loginSuccess(USERNAME);
        verify(repository).findByUsername(USERNAME);
        verify(userProfileService).checkPassword(PASSWORD, ENCODED_PASSWORD);
        verify(jwtService).generateToken(USERNAME);
    }

    @Test
    void authenticate_shouldThrowLockedException_whenUserIsLocked() {
        doThrow(new LockedException("locked for 5 minutes")).when(bruteForceProtectionService).checkIfLocked(USERNAME);

        assertThrows(LockedException.class, () -> service.authenticate(buildAuthRequest()));

        verify(repository, never()).findByUsername(any());
    }

    @Test
    void authenticate_shouldThrowEntityNotFoundException_whenUserNotFound() {
        doNothing().when(bruteForceProtectionService).checkIfLocked(USERNAME);
        when(repository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.authenticate(buildAuthRequest()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
        verify(bruteForceProtectionService, never()).loginFailed(any());
    }

    @Test
    void authenticate_shouldThrowBadCredentialsException_whenPasswordInvalid() {
        doNothing().when(bruteForceProtectionService).checkIfLocked(USERNAME);
        when(repository.findByUsername(USERNAME)).thenReturn(Optional.of(buildUser()));
        when(userProfileService.checkPassword(INVALID_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        assertThatThrownBy(() -> service.authenticate(buildAuthRequestWithInvalidPassword()))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid credentials");
        verify(bruteForceProtectionService).loginFailed(USERNAME);
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void logout_shouldBlacklistTokenAndClearContext_whenHeaderIsValid() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN);
        when(jwtService.extractUsername(TOKEN)).thenReturn(USERNAME);

        service.logout(request);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtService).extractUsername(TOKEN);
        verify(tokenBlacklistService).blacklist(TOKEN);
        SecurityContextHolder.clearContext();
    }

    @Test
    void logout_shouldThrowException_whenHeaderIsMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertThatThrownBy(() -> service.logout(request))
                .isInstanceOf(UserAuthenticationException.class)
                .hasMessage(INVALID_HEADER_ERROR);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(tokenBlacklistService);
    }

    @Test
    void logout_shouldThrowException_whenHeaderIsMalformed() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + TOKEN);

        assertThatThrownBy(() -> service.logout(request))
                .isInstanceOf(UserAuthenticationException.class)
                .hasMessage(INVALID_HEADER_ERROR);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(tokenBlacklistService);
    }

    private User buildUser() {
        return User.builder()
                .username(USERNAME)
                .password(ENCODED_PASSWORD)
                .isActive(true)
                .build();
    }

    private AuthRequestDTO buildAuthRequest() {
        return AuthRequestDTO.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .build();
    }

    private AuthRequestDTO buildAuthRequestWithInvalidPassword() {
        return AuthRequestDTO.builder()
                .username(USERNAME)
                .password(INVALID_PASSWORD)
                .build();
    }
}
