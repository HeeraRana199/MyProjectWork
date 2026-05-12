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
        Map<String, String> errors = new LinkedHashMap<String, String>();

        ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        ErrorResponse response = new ErrorResponse();
        response.setMessage(errors.toString());
        response.setCode(404);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
}
