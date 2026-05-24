package com.gym.crm.auth;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import com.gym.crm.exception.UserAuthenticationException;
import com.gym.crm.exception.UserAuthorizationException;
import com.gym.crm.model.User;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
public class AuthenticationAspect {
    private final SessionContext sessionContext;

    @Before(value = "@annotation(com.gym.crm.auth.Authenticated) && args(.., username)", argNames = "username")
    public void checkAuthentication(String username) {
        User user = sessionContext.getAuthenticatedUser();

        Optional.ofNullable(user)
                .orElseThrow(() -> new UserAuthenticationException("No user authenticated"));

        Optional.ofNullable(username)
                .orElseThrow(() -> new UserAuthenticationException("User is not authenticated: no request to check authentication"));

        if (!user.getUsername().equals(username)) {
            throw new UserAuthorizationException(String.format("Authenticated user with username: %s does not match with requested user with username: %s", user.getUsername(), username));
        }
    }
}
