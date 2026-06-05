package com.gym.crm.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static com.gym.crm.util.TestConstants.USERNAME;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
    private static final String JWT_SECRET = "QWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXo1Njc4OTAxMjM0NTY3OA==";
    private static final long JWT_EXPIRATION_MS = 3600000;

    private final JwtService service = new JwtService();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "jwtSecret", "dGVzdC1zZWNyZXQta2V5LWZvci1qdW5pdC10ZXN0cy10ZXN0LXNlY3JldC1rZXk=");
        ReflectionTestUtils.setField(service, "jwnExpirationMs", 3600000L);
    }

    @Test
    void extractExpiration_shouldReturnCorrectDate() {
        long now = System.currentTimeMillis();
        String token = service.generateToken(USERNAME);
        Date expected = new Date(now + JWT_EXPIRATION_MS);

        Date actual = service.extractExpiration(token);

        assertThat(actual.getTime()).isCloseTo(expected.getTime(), within(2000L));
    }

    @Test
    void generateToken_shouldReturnNonNullToken() {
        String token = service.generateToken("tom.tomas");

        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void extractUsername_shouldReturnCorrectUsername() {
        String token = service.generateToken("tom.tomas");

        String username = service.extractUsername(token);

        assertThat(username).isEqualTo("tom.tomas");
    }

    @Test
    void isTokenValid_shouldReturnTrue_whenTokenValid() {
        String token = service.generateToken("tom.tomas");

        boolean result = service.isTokenValid(token);

        assertThat(result).isTrue();
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenInvalid() {
        boolean result = service.isTokenValid("invalid.token.value");

        assertThat(result).isFalse();
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenExpired() {
        ReflectionTestUtils.setField(service, "jwnExpirationMs", -1000L);
        String token = service.generateToken("tom.tomas");

        boolean result = service.isTokenValid(token);

        assertThat(result).isFalse();
    }
}