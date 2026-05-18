package org.gym.crm.exception;

import static java.lang.String.format;

public class CoreValidationException extends RuntimeException  {
    private static final int MAX_USERNAME_LENGTH = 110;

    public CoreValidationException(String username) {
        super(format(
                "Username '%s' exceeds maximum length of %d characters (got %d)",
                username, MAX_USERNAME_LENGTH, username.length()
        ));
    }
}
