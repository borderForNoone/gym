package org.gym.crm.service.common;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;
import org.gym.crm.exception.ValidationFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserInputValidatorTest {
    private Validator validator;
    private UserInputValidator userInputValidator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        userInputValidator = new UserInputValidator(validator);
    }

    static class TestObject {
        @NotNull(message = "must not be null")
        private String field;

        TestObject(String field) {
            this.field = field;
        }
    }

    @Test
    void validate_shouldThrow_whenObjectIsNull() {
        ValidationFailedException ex = assertThrows(
                ValidationFailedException.class,
                () -> userInputValidator.validate(null, "TestObject")
        );

        assertEquals("TestObject cannot be null", ex.getMessage());
    }

    @Test
    void validate_shouldPass_whenNoViolations() {
        TestObject obj = new TestObject("value");

        assertDoesNotThrow(() ->
                userInputValidator.validate(obj, "TestObject")
        );
    }

    @Test
    void validate_shouldThrow_whenViolationsExist() {
        TestObject obj = new TestObject(null);

        ValidationFailedException ex = assertThrows(
                ValidationFailedException.class,
                () -> userInputValidator.validate(obj, "TestObject")
        );

        assertTrue(ex.getMessage().startsWith("Validation failed:"));
        assertTrue(ex.getMessage().contains("field"));
        assertTrue(ex.getMessage().contains("must not be null"));
    }

    @Test
    void validateUsername_shouldThrow_whenNull() {
        ValidationFailedException ex = assertThrows(
                ValidationFailedException.class,
                () -> userInputValidator.validateUsername(null)
        );

        assertEquals("Username cannot be null or empty", ex.getMessage());
    }

    @Test
    void validateUsername_shouldThrow_whenBlank() {
        ValidationFailedException ex = assertThrows(
                ValidationFailedException.class,
                () -> userInputValidator.validateUsername("   ")
        );

        assertEquals("Username cannot be null or empty", ex.getMessage());
    }

    @Test
    void validateUsername_shouldPass_whenValid() {
        assertDoesNotThrow(() ->
                userInputValidator.validateUsername("john")
        );
    }

    @Test
    void validateId_shouldThrow_whenNull() {
        ValidationFailedException ex = assertThrows(
                ValidationFailedException.class,
                () -> userInputValidator.validateId(null)
        );

        assertEquals("ID cannot be null", ex.getMessage());
    }

    @Test
    void validateId_shouldThrow_whenZero() {
        ValidationFailedException ex = assertThrows(
                ValidationFailedException.class,
                () -> userInputValidator.validateId(0L)
        );

        assertEquals("ID must be a positive number", ex.getMessage());
    }

    @Test
    void validateId_shouldThrow_whenNegative() {
        ValidationFailedException ex = assertThrows(
                ValidationFailedException.class,
                () -> userInputValidator.validateId(-5L)
        );

        assertEquals("ID must be a positive number", ex.getMessage());
    }

    @Test
    void validateId_shouldPass_whenValid() {
        assertDoesNotThrow(() ->
                userInputValidator.validateId(10L)
        );
    }

    @Test
    void validateNotBlank_shouldThrow_whenNull() {
        ValidationFailedException ex = assertThrows(
                ValidationFailedException.class,
                () -> userInputValidator.validateNotBlank(null, "Field")
        );

        assertEquals("Field cannot be null", ex.getMessage());
    }

    @Test
    void validateNotBlank_shouldThrow_whenEmpty() {
        ValidationFailedException ex = assertThrows(
                ValidationFailedException.class,
                () -> userInputValidator.validateNotBlank("   ", "Field")
        );

        assertEquals("Field cannot be empty", ex.getMessage());
    }

    @Test
    void validateNotBlank_shouldPass_whenValid() {
        assertDoesNotThrow(() ->
                userInputValidator.validateNotBlank("value", "Field")
        );
    }
}