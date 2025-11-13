package com.oneday.exception;

import com.oneday.config.ErrorMessageProperties;
import com.oneday.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application.
 * Provides consistent error responses across all endpoints.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ErrorMessageProperties errorMessages;

    public GlobalExceptionHandler(ErrorMessageProperties errorMessages) {
        this.errorMessages = errorMessages;
    }

    /**
     * Handle IllegalArgumentException - validation errors from business logic.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @SuppressWarnings("unused")  // Used by Spring framework via @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Validation error: {}", e.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse(errorMessages.getValidation(), e.getMessage()));
    }

    /**
     * Handle validation errors from @Valid annotation.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @SuppressWarnings("unused")  // Used by Spring framework via @ExceptionHandler
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException exception) {
        Map<String, String> validationErrors = extractValidationErrors(exception);
        log.warn("Validation errors: {}", validationErrors);
        return ResponseEntity.badRequest().body(validationErrors);
    }

    /**
     * Handle all other unexpected exceptions.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @SuppressWarnings("unused")  // Used by Spring framework via @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(errorMessages.getInternalServer(), errorMessages.getUnexpected()));
    }

    /**
     * Extract field-level validation errors from BindingResult.
     *
     * @param exception the MethodArgumentNotValidException containing binding errors
     * @return map of field names to error messages
     */
    private Map<String, String> extractValidationErrors(MethodArgumentNotValidException exception) {
        Map<String, String> validationErrors = new HashMap<>();

        exception.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = getFieldName(error);
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        return validationErrors;
    }

    /**
     * Extract field name from error object.
     *
     * @param error the ObjectError from binding result
     * @return field name or object name if not a field error
     */
    private String getFieldName(Object error) {
        if (error instanceof FieldError) {
            return ((FieldError) error).getField();
        }
        return error.toString();
    }
}
