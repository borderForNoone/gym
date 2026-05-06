package org.gym.crm.util;

import org.gym.crm.exception.UsernameTooLongException;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CoreValidator {
    private static final int MAX_USERNAME_LENGTH = 110;
    private static final String INVALID_ID_MESSAGE = "ID must be positive and not null, got: %s";
    private static final String BLANK_FIELD_MESSAGE = "%s cannot be null or empty";
    private static final String NULL_OBJECT_MESSAGE = "%s cannot be null";

    public void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(String.format(INVALID_ID_MESSAGE, id));
        }
    }

    public void validateNotNull(Object object, String objectName) {
        if (object == null) {
            throw new IllegalArgumentException(String.format(NULL_OBJECT_MESSAGE, objectName));
        }
    }

    public void validateNotBlank(String value, String fieldName) {
        if (Objects.isNull(value) || value.isBlank()) {
            throw new IllegalArgumentException(String.format(BLANK_FIELD_MESSAGE, fieldName));
        }
    }

    public void validateUsernameLength(String username) {
        validateNotBlank(username, "Username");
        if (username.length() > MAX_USERNAME_LENGTH) {
            throw new UsernameTooLongException(username);
        }
    }
}
