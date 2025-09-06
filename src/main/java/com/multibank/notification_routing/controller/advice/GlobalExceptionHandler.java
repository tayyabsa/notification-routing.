package com.multibank.notification_routing.controller.advice;

import com.multibank.notification_routing.exception.ApplicationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(ApplicationException ex) {

        if (ex.getCode().equals("404")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Runtime Exception Occurred");
            errorResponse.put("message", ex.getMessage());
            errorResponse.put("status", HttpStatus.NOT_FOUND.value());
            errorResponse.put("timestamp", System.currentTimeMillis());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Bad Request");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("timestamp", System.currentTimeMillis());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}