package com.chubb.booking.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {

        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        Map<String, Object> body = Map.of(
                "code", status.value() + " " + status.getReasonPhrase(),
                "message", ex.getReason() != null ? ex.getReason() : ex.getMessage()
        );

        return ResponseEntity.status(status).body(body);
    }

    // Validation errors (optional)
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<Map<String, Object>> handleBindException(WebExchangeBindException ex) {

        String msg = ex.getAllErrors().stream()
                .map(err -> err.getDefaultMessage())
                .findFirst()
                .orElse("validation.error");

        Map<String, Object> body = Map.of(
                "code", HttpStatus.BAD_REQUEST.value() + " BAD_REQUEST",
                "message", msg
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // Fallback for all uncaught exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {

        Map<String, Object> body = Map.of(
                "code", HttpStatus.INTERNAL_SERVER_ERROR.value() + " INTERNAL_ERROR",
                "message", ex.getMessage() != null ? ex.getMessage() : "internal.error"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}