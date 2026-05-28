package com.cts.exceptions;

import com.cts.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CandidateNotFoundException.class)
    public ResponseEntity<?> handleCandidateNotFoundException(CandidateNotFoundException ex){
        ErrorResponse response = new ErrorResponse();
        response.setMessage(ex.getMessage());
        response.setCode(404);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        // Build a human-readable message like:
        //   "password: Password must be at least 6 characters long; email: must be a valid email address"
        // (the previous version emitted a raw Java Map toString such as
        //  "{password=Password should not be empty}" which leaked into the UI.)
        Map<String, String> errorsMap = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errorsMap.putIfAbsent(error.getField(), error.getDefaultMessage()));

        String message = errorsMap.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(java.util.stream.Collectors.joining("; "));

        ErrorResponse response = new ErrorResponse();
        response.setMessage(message.isEmpty() ? "Validation failed" : message);
        response.setCode(400);
        // Validation failures are 400 Bad Request, not 404. The old handler
        // returned 404 which caused the frontend to misinterpret the failure.
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
