package org.gym.crm.auth;

import org.gym.crm.model.User;
import org.springframework.stereotype.Component;

@Component
public class SessionContext {
    private static final ThreadLocal<User> AUTH_USER = new ThreadLocal<>();

    public void setAuthenticatedUser(User user) {
        AUTH_USER.set(user);
    }

    public User getAuthenticatedUser() {
        return AUTH_USER.get();
    }

    public void clear() {
        AUTH_USER.remove();
    }
}
