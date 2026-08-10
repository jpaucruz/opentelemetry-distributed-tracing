package com.jpaucruz.observability.infrastructure.adapter.in.rest.exception;

import com.jpaucruz.observability.application.exception.InsufficientStockException;
import com.jpaucruz.observability.application.exception.InventoryNotFoundException;
import com.jpaucruz.observability.generated.adapter.in.rest.model.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InventoryNotFoundException.class)
    ResponseEntity<ErrorResponse> handleInventoryNotFound(InventoryNotFoundException exception, HttpServletRequest request) {
        return buildErrorResponse(
            HttpStatus.NOT_FOUND,
            "INVENTORY_NOT_FOUND",
            exception.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(InsufficientStockException.class)
    ResponseEntity<ErrorResponse> handleInsufficientStock(InsufficientStockException exception, HttpServletRequest request) {
        return buildErrorResponse(
            HttpStatus.CONFLICT,
            "INSUFFICIENT_STOCK",
            exception.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentNotValidException.class
    })
    ResponseEntity<ErrorResponse> handleMethodValidation(Exception exception, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST",
                "One or more request parameters are missing or invalid",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleUnexpectedException(Exception exception, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                request.getRequestURI()
        );
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String code, String message, String path) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setCode(code);
        errorResponse.setMessage(message);
        errorResponse.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        errorResponse.setPath(path);
        return ResponseEntity.status(status).body(errorResponse);
    }

}
