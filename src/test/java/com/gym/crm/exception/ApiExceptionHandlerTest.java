package com.gym.crm.exception;

import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.gym.crm.rest.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiExceptionHandlerTest {
    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void handleValidationFailedException_shouldReturnErrorResponse() {
        ValidationFailedException exception = new ValidationFailedException("Date of birth must be in the past");

        ResponseEntity<ErrorResponse> response = handler.handleValidationFailedException(exception);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ApiError.VALIDATION_ERROR.getCode());
        assertThat(response.getBody().getErrorMessage()).isEqualTo("Validation error");
    }

    @Test
    void handleMethodArgumentNotValidException_shouldReturnErrorResponse() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError firstNameError = new FieldError("traineeCreateRequest", "firstName", "must not be blank");
        FieldError lastNameError = new FieldError("traineeCreateRequest", "lastName", "must not be blank");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(firstNameError, lastNameError));

        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentNotValidException(exception);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ApiError.VALIDATION_ERROR.getCode());
        assertThat(response.getBody().getErrorMessage()).isEqualTo("Validation error");
    }

    @Test
    void handleConstraintViolationException_shouldReturnErrorResponse() {
        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
        Set<ConstraintViolation<?>> violations = Set.of(violation1, violation2);
        ConstraintViolationException exception = new ConstraintViolationException(violations);

        when(violation1.getMessage()).thenReturn("Username must not be blank");
        when(violation2.getMessage()).thenReturn("ID must be positive");

        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolationException(exception);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ApiError.VALIDATION_ERROR.getCode());
        assertThat(response.getBody().getErrorMessage()).isEqualTo("Validation error");
    }

    @Test
    void handleBadCredentialsException_shouldReturnErrorResponse() {
        BadCredentialsException exception = new BadCredentialsException("Invalid credentials for user");

        ResponseEntity<ErrorResponse> response = handler.handleBadCredentialsException(exception);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ApiError.AUTHENTICATION_ERROR.getCode());
        assertThat(response.getBody().getErrorMessage()).isEqualTo("Authentication fails");
    }

    @Test
    void handleUserAuthenticationException_shouldReturnErrorResponse() {
        UserAuthenticationException exception = new UserAuthenticationException("No user authenticated");

        ResponseEntity<ErrorResponse> response = handler.handleUserAuthenticationException(exception);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ApiError.AUTHENTICATION_ERROR.getCode());
        assertThat(response.getBody().getErrorMessage()).isEqualTo("Authentication fails");
    }

    @Test
    void handleLockedException_shouldReturnErrorResponse() {
        LockedException exception = new LockedException("User is locked for 5 minutes due to too many failed login attempts");

        ResponseEntity<ErrorResponse> response = handler.handleLockedException(exception);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ApiError.AUTHENTICATION_ERROR.getCode());
        assertThat(response.getBody().getErrorMessage()).isEqualTo("Authentication fails");
    }

    @Test
    void handleUserAuthorizationException_shouldReturnErrorResponse() {
        UserAuthorizationException exception = new UserAuthorizationException("Authenticated user with username: username does not match with requested user with username");

        ResponseEntity<ErrorResponse> response = handler.handleUserAuthorizationException(exception);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().value()).isEqualTo(403);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ApiError.AUTHORIZATION_ERROR.getCode());
        assertThat(response.getBody().getErrorMessage()).isEqualTo("User is not authorized for request operation");
    }

    @Test
    void handleAccessDeniedException_shouldReturnErrorResponse() {
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        ResponseEntity<ErrorResponse> response = handler.handleAccessDeniedException(exception);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().value()).isEqualTo(403);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ApiError.AUTHORIZATION_ERROR.getCode());
        assertThat(response.getBody().getErrorMessage()).isEqualTo("User is not authorized for request operation");
    }

    @Test
    void handleEntityNotFoundException_shouldReturnErrorResponse() {
        EntityNotFoundException exception = new EntityNotFoundException("User not found");

        ResponseEntity<ErrorResponse> response = handler.handleEntityNotFoundException(exception);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ApiError.NOT_FOUND_ERROR.getCode());
        assertThat(response.getBody().getErrorMessage()).isEqualTo("Requested data was not found");
    }

    @Test
    void handlePersistenceException_shouldReturnErrorResponse() {
        PersistenceException exception = new PersistenceException();

        ResponseEntity<ErrorResponse> response = handler.handlePersistenceException(exception);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ApiError.DATABASE_ERROR.getCode());
        assertThat(response.getBody().getErrorMessage()).isEqualTo("Unexpected database access failure");
    }

    @Test
    void handleGeneralException_shouldReturnErrorResponse() {
        Exception exception = new Exception();

        ResponseEntity<ErrorResponse> response = handler.handleGeneralException(exception);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(ApiError.SERVICE_ERROR.getCode());
        assertThat(response.getBody().getErrorMessage()).isEqualTo("Internal processing error");
    }
}
