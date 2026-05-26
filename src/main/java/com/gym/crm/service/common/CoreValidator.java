package com.gym.crm.service.common;

import com.gym.crm.exception.CoreValidationException;
import com.gym.crm.facade.dto.PasswordChangeRequest;
import com.gym.crm.model.FieldName;
import com.gym.crm.model.Trainee;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Component
public class CoreValidator {
    private static final int MAX_FIRST_NAME_LENGTH = 50;
    private static final int MAX_LAST_NAME_LENGTH = 50;
    private static final String BLANK_FIELD_MESSAGE = "%s cannot be null or empty";
    private static final String NULL_OBJECT_MESSAGE = "%s cannot be null";
    private static final int MAX_ADDRESS_LENGTH = 255;

    private final AtomicReference<Validator> jakartaValidator = new AtomicReference<>();

    private Validator getJakartaValidator() {
        return jakartaValidator.updateAndGet(existing -> {
            if (existing != null) {
                return existing;
            }
            try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
                return factory.getValidator();
            }
        });
    }

    public void validateNotNull(Object object, String objectName) {
        if (object == null) {
            throw new IllegalArgumentException(format(NULL_OBJECT_MESSAGE, objectName));
        }
    }

    public static void validateNotBlank(String value, String fieldName) {
        if (Objects.isNull(value) || value.isBlank()) {
            throw new IllegalArgumentException(format(BLANK_FIELD_MESSAGE, fieldName));
        }
    }

    public void validateTextFieldSize(String fieldValue, FieldName fieldName, int maxLength) {
        validateNotBlank(fieldValue, fieldName.toString());

        if (fieldValue.length() > maxLength) {
            throw new CoreValidationException(format("%s cannot exceed %d characters, got: %d", fieldName, maxLength, fieldValue.length()));
        }
    }

    public void validateDateOfBirth(LocalDate dateOfBirth) {
        validateNotNull(dateOfBirth, FieldName.DATE_OF_BIRTH.toString());

        if (dateOfBirth.isAfter(LocalDate.now())) {
            throw new CoreValidationException(format("%s cannot be in the future, got: %s", FieldName.DATE_OF_BIRTH, dateOfBirth));
        }
    }

    public void validateTrainee(Trainee trainee) {
        validateNotNull(trainee, FieldName.TRAINEE.toString());
        validateNotNull(trainee.getUser(), FieldName.USER.toString());

        validateTextFieldSize(trainee.getUser().getFirstName(), FieldName.FIRST_NAME, MAX_FIRST_NAME_LENGTH);
        validateTextFieldSize(trainee.getUser().getLastName(), FieldName.LAST_NAME, MAX_LAST_NAME_LENGTH);

        if (trainee.getAddress() != null) {
            validateTextFieldSize(trainee.getAddress(), FieldName.ADDRESS, MAX_ADDRESS_LENGTH);
        }

        if (trainee.getDateOfBirth() != null) {
            validateDateOfBirth(trainee.getDateOfBirth());
        }
    }

    public void validate(PasswordChangeRequest request, String objectName) {
        validateNotNull(request, objectName);

        Set<ConstraintViolation<PasswordChangeRequest>> violations = getJakartaValidator().validate(request);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                    .collect(Collectors.joining(", "));
            throw new CoreValidationException(objectName + " is invalid: " + message);
        }
    }
}