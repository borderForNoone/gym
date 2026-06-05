package com.gym.crm.service.common;

import com.gym.crm.exception.UserAuthenticationException;
import com.gym.crm.facade.dto.AuthRequestDTO;
import com.gym.crm.facade.dto.AuthResponseDTO;
import com.gym.crm.model.User;
import com.gym.crm.repository.UserRepository;
import com.gym.crm.security.JwtService;
import com.gym.crm.security.TokenBlacklistService;
import com.gym.crm.service.UserProfileService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class AuthenticationService {
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String INVALID_HEADER_ERROR = "Missing or malformed Authorization header";

    private final UserRepository repository;
    private final UserProfileService service;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @Transactional(readOnly = true)
    public AuthResponseDTO authenticate(@Valid AuthRequestDTO dto) {
        log.info("Authentication attempt for user: {}", dto.getUsername());

        User user = repository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + dto.getUsername()));
        if (!service.checkPassword(dto.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials for user: " + dto.getUsername());
        }

        log.info("Authentication successful for user: {}", dto.getUsername());

        return AuthResponseDTO.builder()
                .username(user.getUsername())
                .token(jwtService.generateToken(user.getUsername()))
                .build();
    }

    public void logout(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw new UserAuthenticationException(INVALID_HEADER_ERROR);
        }

        String token = header.substring(BEARER_PREFIX.length());
        String username = jwtService.extractUsername(token);
        log.info("Logging out user: username={}", username);

        tokenBlacklistService.blacklist(token);
        SecurityContextHolder.clearContext();
        log.info("User logged out successfully: username{}", username);
    }
}
