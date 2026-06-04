 package com.hrportal.exception;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.hrportal.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@RestControllerAdvice
  public class GlobalExceptionHandler {

      @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                  .body(ApiResponse.error(ex.getMessage()));
      }

      @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                  .body(ApiResponse.error("Internal server error: " + ex.getMessage()));
      }

       @ExceptionHandler(DuplicateResourceException.class)
        public ResponseEntity<ApiResponse<Void>> handleDuplicate(DuplicateResourceException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)  
              .body(ApiResponse.error(ex.getMessage()));
        }

        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
              .body(ApiResponse.error(ex.getMessage()));
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof UnrecognizedPropertyException upe) {
          return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error("Unknown field: " + upe.getPropertyName()));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error("Malformed request body"));
        }
  }