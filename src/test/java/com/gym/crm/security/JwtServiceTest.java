package com.gym.crm.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
    @InjectMocks
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "dGVzdC1zZWNyZXQta2V5LWZvci1qdW5pdC10ZXN0cy10ZXN0LXNlY3JldC1rZXk=");
        ReflectionTestUtils.setField(jwtService, "jwnExpirationMs", 3600000L);
    }

    @Test
    void generateToken_shouldReturnNonNullToken() {
        String token = jwtService.generateToken("tom.tomas");

        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void extractUsername_shouldReturnCorrectUsername() {
        String token = jwtService.generateToken("tom.tomas");

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("tom.tomas");
    }

    @Test
    void isTokenValid_shouldReturnTrue_whenTokenValid() {
        String token = jwtService.generateToken("tom.tomas");

        boolean result = jwtService.isTokenValid(token);

        assertThat(result).isTrue();
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenInvalid() {
        boolean result = jwtService.isTokenValid("invalid.token.value");

        assertThat(result).isFalse();
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenExpired() {
        ReflectionTestUtils.setField(jwtService, "jwnExpirationMs", -1000L);
        String token = jwtService.generateToken("tom.tomas");

        boolean result = jwtService.isTokenValid(token);

        assertThat(result).isFalse();
    }
}