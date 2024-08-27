package com.erkan.weather_api.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<String> handleExternalApiException(ExternalApiException ex) {
        log.error("External API error: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Error occurred while calling external API: " + ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Parameter error: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error occurred for parameters: " + ex.getMessage());
    }
}
