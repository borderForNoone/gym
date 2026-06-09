package com.gym.crm.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HexFormat;
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

        String tokenHash = hashToken(token);
        template.opsForValue().set("blacklist:" + tokenHash,
                "true",
                remainingTime,
                TimeUnit.MILLISECONDS);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    public boolean isBlacklisted(String token) {
        return template.hasKey("blacklist:" + token);
    }
}