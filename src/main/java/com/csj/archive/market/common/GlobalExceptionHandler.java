package com.csj.archive.market.common;

import java.util.List;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ErrorResponse> notFound(NotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.code(), ex.getMessage(), List.of());
    }

    @ExceptionHandler(DuplicateEventException.class)
    ResponseEntity<ErrorResponse> duplicate(DuplicateEventException ex) {
        return error(HttpStatus.CONFLICT, ex.code(), ex.getMessage(), List.of());
    }

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ErrorResponse> business(BusinessException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.code(), ex.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::format)
                .toList();
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", details);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> unexpected(Exception ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage(), List.of());
    }

    private String format(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String code, String message, List<String> details) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(code, message, MDC.get(TraceIdFilter.TRACE_ID), details));
    }
}
