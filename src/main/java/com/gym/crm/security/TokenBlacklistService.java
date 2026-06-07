package com.gym.crm.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
    private final StringRedisTemplate template;
    private final JwtService jwtService;

    public void blacklist(String token) {
        Date expirationDate = jwtService.extractExpiration(token);
        long remainingTime = expirationDate.getTime() - System.currentTimeMillis();

        if (remainingTime <= 0) {
            return;
        }

        template.opsForValue().set("blacklist:" + token,
                "true",
                remainingTime,
                TimeUnit.MILLISECONDS);
    }

    public boolean isBlacklisted(String token) {
        return template.hasKey("blacklist:" + token);
    }
}