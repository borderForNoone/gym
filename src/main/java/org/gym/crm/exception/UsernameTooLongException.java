package org.gym.crm.exception;

public class UsernameTooLongException extends RuntimeException  {
    private static final int MAX_USERNAME_LENGTH = 110;

    public UsernameTooLongException(String username) {
        super(String.format(
                "Username '%s' exceeds maximum length of %d characters (got %d)",
                username, MAX_USERNAME_LENGTH, username.length()
        ));
    }
}
