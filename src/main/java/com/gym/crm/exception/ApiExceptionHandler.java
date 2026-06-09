package com.gym.crm.exception;

import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.gym.crm.rest.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
    public ResponseEntity<ErrorResponse> handleLockedException(LockedException exception) {
        log.warn("User authentication failed: {}", exception.getMessage());

        return buildErrorResponse(AUTHENTICATION_ERROR);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException exception) {
        log.warn("User is not authorized for request operation: {}", exception.getMessage());

        return buildErrorResponse(AUTHORIZATION_ERROR);
    }

    @ExceptionHandler(ValidationFailedException.class)
    public ResponseEntity<ErrorResponse> handleValidationFailedException(ValidationFailedException exception) {
        log.warn(VALIDATION_ERROR_LOG_MESSAGE, exception.getMessage());

        return buildErrorResponse(VALIDATION_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String errorMessage = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Validation error: {}", errorMessage);

        return buildErrorResponse(VALIDATION_ERROR);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException exception) {
        log.warn("Bad credentials: {}", exception.getMessage());

        return buildErrorResponse(AUTHENTICATION_ERROR);
    }

    @ExceptionHandler(UserAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleUserAuthenticationException(UserAuthenticationException exception) {
        log.warn("User authentication failed: {}", exception.getMessage());

        return buildErrorResponse(AUTHENTICATION_ERROR);
    }

    @ExceptionHandler(UserAuthorizationException.class)
    public ResponseEntity<ErrorResponse> handleUserAuthorizationException(UserAuthorizationException exception) {
        log.warn("User is not authorized for request operation: {}", exception.getMessage());

        return buildErrorResponse(AUTHORIZATION_ERROR);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException exception) {
        log.warn("Requested data was not found: {}", exception.getMessage());

        return buildErrorResponse(NOT_FOUND_ERROR);
    }

    @ExceptionHandler(PersistenceException.class)
    public ResponseEntity<ErrorResponse> handlePersistenceException(PersistenceException exception) {
        log.error("Database access failure:", exception);

        return buildErrorResponse(DATABASE_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception exception) {
        log.error("Unhandled exception occurred", exception);

        return buildErrorResponse(SERVICE_ERROR);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException exception) {
        String errorMessage = exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn(VALIDATION_ERROR_LOG_MESSAGE, errorMessage);

        return buildErrorResponse(VALIDATION_ERROR);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(ApiError apiError) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode(apiError.getCode());
        errorResponse.setErrorMessage(apiError.getMessage());

        return new ResponseEntity<>(errorResponse, apiError.getStatus());
    }
}
