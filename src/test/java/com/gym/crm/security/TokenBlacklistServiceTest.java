package com.gym.crm.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {
    private static final String TOKEN = "token";
    private static final String BLACKLIST_KEY = "blacklist:";

    @Mock
    private StringRedisTemplate template;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void isBlacklisted_shouldReturnFalse_whenTokenNotBlacklisted() {
        when(template.hasKey("blacklist:" + TOKEN)).thenReturn(false);

        boolean actual = tokenBlacklistService.isBlacklisted(TOKEN);

        assertThat(actual).isFalse();
        verify(template).hasKey("blacklist:" + TOKEN);
    }

    @Test
    void isBlacklisted_shouldReturnTrue_whenTokenBlacklisted() {
        when(template.hasKey("blacklist:" + TOKEN)).thenReturn(true);

        boolean actual = tokenBlacklistService.isBlacklisted(TOKEN);

        assertThat(actual).isTrue();
        verify(template).hasKey("blacklist:" + TOKEN);
    }

    @Test
    void blacklist_shouldAddTokenToBlacklist_whenCorrectTime() {
        String hashToken = ReflectionTestUtils.invokeMethod(tokenBlacklistService, "hashToken", TOKEN);
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(template.opsForValue()).thenReturn(valueOps);
        when(jwtService.extractExpiration(TOKEN)).thenReturn(new Date(System.currentTimeMillis() + 10000));

        tokenBlacklistService.blacklist(TOKEN);

        verify(template).opsForValue();
        verify(valueOps).set(eq(BLACKLIST_KEY + hashToken),
                eq("true"),
                anyLong(),
                eq(TimeUnit.MILLISECONDS));
    }

    @Test
    void blacklist_shouldNotAddToken_whenExpired() {
        when(jwtService.extractExpiration(TOKEN)).thenReturn(new Date(System.currentTimeMillis() - 1000));

        tokenBlacklistService.blacklist(TOKEN);

        verify(template, never()).opsForValue();
    }
}
