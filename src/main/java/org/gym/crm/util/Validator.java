package org.gym.crm.util;

import java.util.Objects;

public class Validator {
    private static final String INVALID_ID_EXCEPTION_MESSAGE = "ID must be positive and not null, got: %s";
    private static final String EMPTY_STRING_EXCEPTION_MESSAGE = "%s cannot be null or empty";
    private static final String NULL_OBJECT_EXCEPTION_MESSAGE = "%s cannot be null";

    public static void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(String.format(INVALID_ID_EXCEPTION_MESSAGE, id));
        }
    }

    public static void validateNotNull(Object object, String objectName) {
        if (object == null) {
            throw new IllegalArgumentException(String.format(NULL_OBJECT_EXCEPTION_MESSAGE, objectName));
        }
    }

    public static void validateNotBlank(String value, String fieldName) {
        if (Objects.isNull(value) || value.isBlank()) {
            throw new IllegalArgumentException(String.format(EMPTY_STRING_EXCEPTION_MESSAGE, fieldName));
        }
    }
}
