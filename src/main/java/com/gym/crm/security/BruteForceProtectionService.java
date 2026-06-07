package com.gym.crm.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;

import static java.util.concurrent.TimeUnit.MINUTES;

@Slf4j
@Service
@RequiredArgsConstructor
public class BruteForceProtectionService {
    private static final int MAX_ATTEMPTS = 3;
    private static final long LOCK_DURATION_MINUTES = 5;
    private static final String ATTEMPTS_PREFIX = "login:attempts:";
    private static final String LOCK_PREFIX = "login:lock:";

    private final StringRedisTemplate template;

    public void loginSuccess(String username) {
        template.delete(ATTEMPTS_PREFIX + username);
        template.delete(LOCK_PREFIX + username);
    }

    public void loginFailed(String username) {
        Long attempts = template.opsForValue().increment(ATTEMPTS_PREFIX + username);
        template.expire(ATTEMPTS_PREFIX + username, LOCK_DURATION_MINUTES, MINUTES);

        if (attempts == null || attempts < MAX_ATTEMPTS) {
            return;
        }

        template.opsForValue().set(LOCK_PREFIX + username, "locked", LOCK_DURATION_MINUTES, MINUTES);
    }

    public void checkIfLocked(String username) {
        Boolean isLocked = template.hasKey(LOCK_PREFIX + username);
        if (isLocked != null && isLocked) {
            throw new LockedException(String.format("User %s is locked for %s minutes due to too many failed login attempts", username, LOCK_DURATION_MINUTES));
        }
    }
}