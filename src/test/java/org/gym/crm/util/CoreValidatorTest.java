package org.gym.crm.util;

import org.gym.crm.dto.PasswordChangeRequest;
import org.gym.crm.exception.CoreValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CoreValidatorTest {
    private CoreValidator coreValidator;

    @BeforeEach
    void setUp() {
        coreValidator = new CoreValidator();
    }

    @Test
    void getJakartaValidator_shouldReturnSameInstanceOnSubsequentCalls() {
        PasswordChangeRequest request = validRequest();

        assertDoesNotThrow(() -> coreValidator.validate(request, "request"));
        assertDoesNotThrow(() -> coreValidator.validate(request, "request"));
    }

    @Test
    void getJakartaValidator_shouldInitializeValidatorLazily() {
        PasswordChangeRequest request = validRequest();

        assertDoesNotThrow(() -> coreValidator.validate(request, "request"));
    }

    @Test
    void validate_shouldNotThrow_whenRequestIsValid() {
        PasswordChangeRequest request = validRequest();

        assertDoesNotThrow(() -> coreValidator.validate(request, "request"));
        assertEquals("Simone.Radcliffe", request.getUsername());
        assertEquals("oldPassword", request.getOldPassword());
        assertEquals("newPassword", request.getNewPassword());
    }

    @Test
    void validate_shouldThrowIllegalArgumentException_whenRequestIsNull() {
        assertThatThrownBy(() -> coreValidator.validate(null, "request"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("request");
    }

    @Test
    void validate_shouldThrowCoreValidationException_whenUsernameIsNull() {
        PasswordChangeRequest request = PasswordChangeRequest.builder()
                .username(null)
                .oldPassword("oldPass")
                .newPassword("newPass")
                .build();

        assertThatThrownBy(() -> coreValidator.validate(request, "request"))
                .isInstanceOf(CoreValidationException.class)
                .hasMessageContaining("request is invalid");
    }

    @Test
    void validate_shouldThrowCoreValidationException_whenOldPasswordIsNull() {
        PasswordChangeRequest request = PasswordChangeRequest.builder()
                .username("user")
                .oldPassword(null)
                .newPassword("newPass")
                .build();

        assertThatThrownBy(() -> coreValidator.validate(request, "request"))
                .isInstanceOf(CoreValidationException.class)
                .hasMessageContaining("request is invalid");
    }

    @Test
    void validate_shouldThrowCoreValidationException_whenNewPasswordIsNull() {
        PasswordChangeRequest request = PasswordChangeRequest.builder()
                .username("user")
                .oldPassword("oldPass")
                .newPassword(null)
                .build();

        assertThatThrownBy(() -> coreValidator.validate(request, "request"))
                .isInstanceOf(CoreValidationException.class)
                .hasMessageContaining("request is invalid");
    }

    @Test
    void validate_shouldIncludeViolationDetailsInExceptionMessage() {
        PasswordChangeRequest request = PasswordChangeRequest.builder()
                .username(null)
                .oldPassword(null)
                .newPassword(null)
                .build();

        assertThatThrownBy(() -> coreValidator.validate(request, "request"))
                .isInstanceOf(CoreValidationException.class)
                .hasMessageContaining("request is invalid:")
                .hasMessageContaining("username")
                .hasMessageContaining("oldPassword")
                .hasMessageContaining("newPassword");
    }

    private PasswordChangeRequest validRequest() {
        return PasswordChangeRequest.builder()
                .username("Simone.Radcliffe")
                .oldPassword("oldPassword")
                .newPassword("newPassword")
                .build();
    }
}