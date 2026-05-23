package org.gym.crm.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Getter
public enum ApiError {
    VALIDATION_ERROR(2760, "Validation error: ", BAD_REQUEST),
    AUTHENTICATION_ERROR(2805, "Authentication fails: ", UNAUTHORIZED),
    AUTHORIZATION_ERROR(2806, "User is not authorized for request operation: ", FORBIDDEN),
    NOT_FOUND_ERROR(2835, "Requested data was not found: ", NOT_FOUND),
    SERVICE_ERROR(3200, "Internal processing error", INTERNAL_SERVER_ERROR),
    DATABASE_ERROR(3358, "Unexpected database access failure", INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus status;

    ApiError(int code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
