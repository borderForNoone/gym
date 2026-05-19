package org.gym.crm.service.common;

import org.gym.crm.exception.ValidationFailedException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserInputValidator {

    private final Validator validator;

    public <T> void validate(T object, String objectName) {
        if (object == null) {
            throw new ValidationFailedException(objectName + " cannot be null");
        }

        Set<ConstraintViolation<T>> violations = validator.validate(object);

        if (violations.isEmpty()) {
            return;
        }

        String errorMessage = violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        throw new ValidationFailedException("Validation failed: " + errorMessage);
    }

    public void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationFailedException("Username cannot be null or empty");
        }
    }

    public void validateId(Long id) {
        if (id == null) {
            throw new ValidationFailedException("ID cannot be null");
        }
        if (id <= 0) {
            throw new ValidationFailedException("ID must be a positive number");
        }
    }

    public void validateNotBlank(String value, String fieldName) {
        if (value == null) {
            throw new ValidationFailedException(fieldName + " cannot be null");
        }
        if (value.trim().isEmpty() || value.isBlank()) {
            throw new ValidationFailedException(fieldName + " cannot be empty");
        }
    }
}
