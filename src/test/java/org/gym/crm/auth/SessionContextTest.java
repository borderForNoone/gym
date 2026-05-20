package org.gym.crm.auth;

import org.gym.crm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class SessionContextTest {

    private SessionContext sessionContext;

    @BeforeEach
    void setUp() {
        sessionContext = new SessionContext();
    }

    @Test
    void setAuthenticatedUser_shouldStoreUserInThreadLocal() {
        User user = mock(User.class);

        sessionContext.setAuthenticatedUser(user);

        User result = sessionContext.getAuthenticatedUser();

        assertNotNull(result);
        assertEquals(user, result);
    }

    @Test
    void getAuthenticatedUser_shouldReturnNull_whenNoUserSet() {
        User result = sessionContext.getAuthenticatedUser();

        assertNull(result);
    }

    @Test
    void clear_shouldRemoveUserFromThreadLocal() {
        User user = mock(User.class);

        sessionContext.setAuthenticatedUser(user);
        assertNotNull(sessionContext.getAuthenticatedUser());

        sessionContext.clear();

        assertNull(sessionContext.getAuthenticatedUser());
    }

    @Test
    void threadLocal_shouldBeIsolatedBetweenThreads() throws InterruptedException {
        User user1 = mock(User.class);
        User user2 = mock(User.class);

        sessionContext.setAuthenticatedUser(user1);

        Thread thread = new Thread(() -> {
            sessionContext.setAuthenticatedUser(user2);
            assertEquals(user2, sessionContext.getAuthenticatedUser());
        });

        thread.start();
        thread.join();

        assertEquals(user1, sessionContext.getAuthenticatedUser());
    }
}