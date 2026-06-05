package com.gym.crm.auth;

import com.gym.crm.exception.UserAuthenticationException;
import com.gym.crm.exception.UserAuthorizationException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
public class AuthenticationAspect {
    @Before(value = "@annotation(com.gym.crm.auth.Authenticated) && args(.., username)", argNames = "username")
    public void checkAuthentication(String username) {
        Optional.ofNullable(username).orElseThrow(() -> new UserAuthenticationException("User is not authenticated: no request to check authentication"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserAuthenticationException("No user authenticated");
        }

        String authenticatedUsername = authentication.getName();
        if (!authenticatedUsername.equals(username)) {
            throw new UserAuthorizationException(String.format("Authenticated user with username: %s does not match with requested user with username: %s", authenticatedUsername, username));
        }
    }
}
