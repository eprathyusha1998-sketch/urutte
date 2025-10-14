package com.urutte.exception;

import com.urutte.dto.AuthResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AuthResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        StringBuilder errorMessage = new StringBuilder();
        for (Map.Entry<String, String> entry : errors.entrySet()) {
            errorMessage.append(entry.getKey()).append(": ").append(entry.getValue()).append("; ");
        }
        
        return ResponseEntity.badRequest().body(
            AuthResponse.builder()
                .success(false)
                .message(errorMessage.toString())
                .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AuthResponse> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            AuthResponse.builder()
                .success(false)
                .message("An error occurred: " + ex.getMessage())
                .build()
        );
    }
}
