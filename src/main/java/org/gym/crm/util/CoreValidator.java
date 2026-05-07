package org.gym.crm.util;

import org.gym.crm.exception.CoreValidationException;
import org.gym.crm.model.FieldName;
import org.gym.crm.model.Trainee;
import org.gym.crm.model.Trainer;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Objects;

@Component
public class CoreValidator {
    private static final int MAX_FIRST_NAME_LENGTH = 50;
    private static final int MAX_LAST_NAME_LENGTH = 50;
    private static final String BLANK_FIELD_MESSAGE = "%s cannot be null or empty";
    private static final String NULL_OBJECT_MESSAGE = "%s cannot be null";
    private static final int MAX_ADDRESS_LENGTH = 255;

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

    public void validateTextFieldSize(String fieldValue, FieldName fieldName, int maxLength) {
        validateNotBlank(fieldValue, fieldName.toString());
        if (fieldValue.length() > maxLength) {
            throw new CoreValidationException(String.format("%s cannot exceed %d characters, got: %s",
                    fieldName, maxLength, fieldValue.length()));
        }
    }

    public void validateDateOfBirth(LocalDate dateOfBirth) {
        validateNotNull(dateOfBirth, FieldName.DATE_OF_BIRTH.toString());
        if (dateOfBirth.isAfter(LocalDate.now())) {
            throw new CoreValidationException(
                    String.format("%s cannot be in the future, got: %s",
                            FieldName.DATE_OF_BIRTH, dateOfBirth));
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

    public void validateTrainer(Trainer trainer) {
        validateNotNull(trainer, FieldName.TRAINER.toString());
        validateNotNull(trainer.getUser(), FieldName.USER.toString());

        validateTextFieldSize(trainer.getUser().getFirstName(), FieldName.FIRST_NAME, MAX_FIRST_NAME_LENGTH);
        validateTextFieldSize(trainer.getUser().getLastName(), FieldName.LAST_NAME, MAX_LAST_NAME_LENGTH);

        validateNotNull(trainer.getSpecialization(), FieldName.SPECIALIZATION.toString());
    }
}
