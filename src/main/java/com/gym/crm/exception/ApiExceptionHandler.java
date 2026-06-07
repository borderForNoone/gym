package com.gym.crm.exception;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.gym.crm.rest.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.security.access.AccessDeniedException;
import java.util.stream.Collectors;

import static com.gym.crm.exception.ApiError.AUTHENTICATION_ERROR;
import static com.gym.crm.exception.ApiError.AUTHORIZATION_ERROR;
import static com.gym.crm.exception.ApiError.DATABASE_ERROR;
import static com.gym.crm.exception.ApiError.NOT_FOUND_ERROR;
import static com.gym.crm.exception.ApiError.SERVICE_ERROR;
import static com.gym.crm.exception.ApiError.VALIDATION_ERROR;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {
    private static final String VALIDATION_ERROR_LOG_MESSAGE = "Validation error: {}";

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLockedException(LockedException ex) {
        log.warn("User authentication failed: {}", ex.getMessage());

        return buildErrorResponse(AUTHENTICATION_ERROR, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("User is not authorized for request operation: {}", ex.getMessage());

        return buildErrorResponse(AUTHORIZATION_ERROR, ex.getMessage());
    }

    @ExceptionHandler(ValidationFailedException.class)
    public ResponseEntity<ErrorResponse> handleValidationFailedException(ValidationFailedException ex) {
        log.warn(VALIDATION_ERROR_LOG_MESSAGE, ex.getMessage());

        return buildErrorResponse(VALIDATION_ERROR, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Validation error: {}", errorMessage);

        return buildErrorResponse(VALIDATION_ERROR, errorMessage);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        log.warn("Bad credentials: {}", ex.getMessage());

        return buildErrorResponse(AUTHENTICATION_ERROR, ex.getMessage());
    }

    @ExceptionHandler(UserAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleUserAuthenticationException(UserAuthenticationException ex) {
        log.warn("User authentication failed: {}", ex.getMessage());

        return buildErrorResponse(AUTHENTICATION_ERROR, ex.getMessage());
    }

    @ExceptionHandler(UserAuthorizationException.class)
    public ResponseEntity<ErrorResponse> handleUserAuthorizationException(UserAuthorizationException ex) {
        log.warn("User is not authorized for request operation: {}", ex.getMessage());

        return buildErrorResponse(AUTHORIZATION_ERROR, ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex) {
        log.warn("Requested data was not found: {}", ex.getMessage());

        return buildErrorResponse(NOT_FOUND_ERROR, ex.getMessage());
    }

    @ExceptionHandler(PersistenceException.class)
    public ResponseEntity<ErrorResponse> handlePersistenceException(PersistenceException ex) {
        log.error("Database access failure:", ex);

        return buildErrorResponse(DATABASE_ERROR, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("Unhandled exception:", ex);

        return buildErrorResponse(SERVICE_ERROR, ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        String errorMessage = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn(VALIDATION_ERROR_LOG_MESSAGE, errorMessage);

        return buildErrorResponse(VALIDATION_ERROR, errorMessage);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(ApiError apiError, String message) {
        message = StringUtils.isBlank(message) ? "" : message;

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode(apiError.getCode());
        errorResponse.setErrorMessage(apiError.getMessage() + message);

        return new ResponseEntity<>(errorResponse, apiError.getStatus());
    }
}
