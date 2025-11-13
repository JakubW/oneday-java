package com.oneday.exception;

import com.oneday.config.ErrorMessageProperties;
import com.oneday.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

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
        String exceptionMessage = e.getMessage();

        // For postal code errors, return message in error field
        if (exceptionMessage != null && exceptionMessage.contains("Postal Code")) {
            return ResponseEntity.badRequest().body(new ErrorResponse(exceptionMessage));
        }

        // For other errors (like altitude), return with both error and message fields
        return ResponseEntity.badRequest().body(new ErrorResponse(errorMessages.getValidation(), exceptionMessage));
    }

    /**
     * Handle validation errors from @Valid annotation.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @SuppressWarnings("unused")  // Used by Spring framework via @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException exception) {
        String validationErrorMessage = extractFirstValidationError(exception);
        log.warn("Validation error: {}", validationErrorMessage);
        return ResponseEntity.badRequest().body(new ErrorResponse(validationErrorMessage));
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
     * Extract the first validation error message.
     *
     * @param exception the MethodArgumentNotValidException containing binding errors
     * @return first validation error message
     */
    private String extractFirstValidationError(MethodArgumentNotValidException exception) {
        return exception.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse(errorMessages.getValidation());
    }
}
