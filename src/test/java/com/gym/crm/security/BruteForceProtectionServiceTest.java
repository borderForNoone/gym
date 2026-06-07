package com.gym.crm.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.LockedException;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BruteForceProtectionServiceTest {
    private static final String USERNAME = "Simone.Radcliff";
    private static final String ATTEMPTS_KEY = "login:attempts:" + USERNAME;
    private static final String LOCK_KEY = "login:lock:" + USERNAME;

    @Mock
    private StringRedisTemplate template;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private BruteForceProtectionService service;

    @Test
    void loginFailed_shouldIncrementAttempts() {
        when(template.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(ATTEMPTS_KEY)).thenReturn(1L);

        service.loginFailed(USERNAME);

        verify(valueOperations).increment(ATTEMPTS_KEY);
        verify(template).expire(ATTEMPTS_KEY, 5L, TimeUnit.MINUTES);
    }

    @Test
    void loginFailed_shouldLockUser_whenMaxAttemptsReached() {
        when(template.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(ATTEMPTS_KEY)).thenReturn(3L);

        service.loginFailed(USERNAME);

        verify(valueOperations).set(LOCK_KEY, "locked", 5L, TimeUnit.MINUTES);
    }

    @Test
    void loginFailed_shouldNotLockUser_whenBelowMaxAttempts() {
        when(template.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(ATTEMPTS_KEY)).thenReturn(2L);

        service.loginFailed(USERNAME);

        verify(valueOperations, never()).set(eq(LOCK_KEY), any(), anyLong(), any());
    }

    @Test
    void loginSucceeded_shouldClearAttemptsAndLock() {
        service.loginSuccess(USERNAME);

        verify(template).delete(ATTEMPTS_KEY);
        verify(template).delete(LOCK_KEY);
    }

    @Test
    void checkIfLocked_shouldThrowLockedException_whenUserIsLocked() {
        when(template.hasKey(LOCK_KEY)).thenReturn(true);

        LockedException exception = assertThrows(LockedException.class, () -> service.checkIfLocked(USERNAME));

        assertThat(exception.getMessage()).contains("locked for 5 minutes");
    }

    @Test
    void checkIfLocked_shouldNotThrow_whenUserIsNotLocked() {
        when(template.hasKey(LOCK_KEY)).thenReturn(false);

        assertDoesNotThrow(() -> service.checkIfLocked(USERNAME));
    }

    @Test
    void checkIfLocked_shouldNotThrow_whenRedisReturnsNull() {
        when(template.hasKey(LOCK_KEY)).thenReturn(null);

        assertDoesNotThrow(() -> service.checkIfLocked(USERNAME));
    }
}
