package com.chubb.booking.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles @Valid validation errors (BookingRequest, Passenger, etc.)
     */
    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ErrorResponse> handleValidationException(WebExchangeBindException ex) {

        // Field-level errors
        String fieldErrors = ex.getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));

        // Bean-level errors (like @AssertTrue)
        String globalErrors = ex.getGlobalErrors().stream()
                .map(err -> err.getDefaultMessage())
                .collect(Collectors.joining("; "));

        String finalMessage;

        if (!fieldErrors.isEmpty() && !globalErrors.isEmpty()) {
            finalMessage = fieldErrors + "; " + globalErrors;
        } else if (!fieldErrors.isEmpty()) {
            finalMessage = fieldErrors;
        } else {
            finalMessage = globalErrors;
        }

        return Mono.just(new ErrorResponse("VALIDATION_ERROR", finalMessage));
    }

    /**
     * Handles business logic exceptions thrown intentionally using:
     * throw new ResponseStatusException(HttpStatus.CONFLICT, "message")
     */
    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        return Mono.just(new ErrorResponse(
                ex.getStatusCode().toString(),
                ex.getReason() != null ? ex.getReason() : "Unknown error"
        ));
    }

    /**
     * Catch-all exception handler — unexpected issues.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ErrorResponse> handleGenericException(Exception ex) {
        return Mono.just(new ErrorResponse(
                "INTERNAL_ERROR",
                ex.getMessage() != null ? ex.getMessage() : "An internal error occurred"
        ));
    }

    public record ErrorResponse(String code, String message) {}
}