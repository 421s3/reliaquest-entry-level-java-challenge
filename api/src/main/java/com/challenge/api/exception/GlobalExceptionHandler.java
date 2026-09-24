package com.challenge.api.exception;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        ErrorResponse error = new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, path);
        return ResponseEntity.status(status).body(error);
    }

    // 404 Not Found
    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEmployeeNotFound(EmployeeNotFoundException ex, WebRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // 400 Bad Request: Parameter type conversion mismatch
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        String targetType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "expected type";
        String message = String.format(
                "Invalid format for parameter '%s': '%s' is not a valid %s", ex.getName(), ex.getValue(), targetType);
        log.warn("Type mismatch error: {}", message);
        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    // 400 Bad Request: Payload Validation Failure (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        List<String> errors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.add(fieldError.getField() + ": " + fieldError.getDefaultMessage());
        }

        String message = String.join("; ", errors);
        if (message.isEmpty()) {
            message = "Validation failed for request payload";
        }

        log.warn("Validation error: {}", message);
        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    // 400 Bad Request: Malformed JSON/request body
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {
        log.warn(
                "Malformed request body: {}",
                ex.getMostSpecificCause().getClass().getSimpleName());
        return buildResponse(HttpStatus.BAD_REQUEST, "Malformed request body", request);
    }

    // 409 Conflict: operation not allowed due to invalid employee state (termination before start date)
    @ExceptionHandler(InvalidEmployeeStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidEmployeeState(
            InvalidEmployeeStateException ex, WebRequest request) {
        log.warn("Invalid employee state: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // Catch-all for unhandled errors/exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUncaughtException(Exception ex, WebRequest request) {
        // Making sure that Spring's own 4xx exceptions are handled properly and not treated as 500 exceptions.
        if (ex instanceof org.springframework.web.ErrorResponse springError) {
            HttpStatus status = HttpStatus.resolve(springError.getStatusCode().value());
            if (status == null) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            String detailMessage = springError.getBody().getDetail();
            if (detailMessage == null || detailMessage.isBlank()) {
                detailMessage = status.getReasonPhrase();
            }

            log.warn(
                    "Spring MVC web exception [{}] on {}: {}",
                    status.value(),
                    request.getDescription(false),
                    detailMessage);
            return buildResponse(status, detailMessage, request);
        }
        log.error("Unhandled exception occurred at {}", request.getDescription(false), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred", request);
    }
}
