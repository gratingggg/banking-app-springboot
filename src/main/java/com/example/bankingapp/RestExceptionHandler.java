package com.example.bankingapp;

import com.example.bankingapp.dto.exception.ErrorResponse;
import com.example.bankingapp.exception.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    @Autowired
    private ObjectMapper objectMapper;

    @ExceptionHandler({AadharNumberAlreadyExistsException.class, UsernameAlreadyExistsException.class,
            EmailAlreadyExistsException.class, PhoneNumberAlreadyExistsException.class, })
    public ResponseEntity<Object> handleAlreadyExists(Exception ex){
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage(), HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler({InvalidCredentialsException.class, EmployeeInactiveException.class})
    public ResponseEntity<Object> handleInvalidCredentials(Exception ex){
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED.value()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("status", "400");
        Map<String, Object> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        map.put("errors", errors);

        return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request){
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "400");
        Map<String, Object> errors = new LinkedHashMap<>();
        errors.put("gender", "Invalid gender. Allowed genders : MALE, FEMALE, TRANSGENDER");
        body.put("errors", errors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
