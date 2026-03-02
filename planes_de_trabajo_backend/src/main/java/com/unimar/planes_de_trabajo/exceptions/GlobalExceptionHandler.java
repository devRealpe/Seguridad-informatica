package com.unimar.planes_de_trabajo.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.unimar.planes_de_trabajo.models.GenericError;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<GenericError> handleRuntimeException(RuntimeException ex) {
        GenericError error = GenericError.builder()
                .mensaje(ex.getMessage())
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericError> handleGenericException(Exception ex) {
        GenericError error = GenericError.builder()
                .mensaje("Error interno del servidor: " + ex.getMessage())
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}