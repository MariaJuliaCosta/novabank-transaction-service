package com.maju.transactionservice.exceptions;

import com.maju.transactionservice.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<?>> handleBadRequestException(BadRequestException e) {

        log.error("Bad Request Exception: {}", e.getMessage());

        ApiResponse<?> errorResponse = new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFoundException(NotFoundException e) {

        log.error("Not Found Exception: {}", e.getMessage());

        ApiResponse<?> errorResponse = new ApiResponse<>(HttpStatus.NOT_FOUND.value(), e.getMessage(), null);

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleOtherException(Exception e) {

        log.error("Other Exception: {}", e.getMessage());

        ApiResponse<?> errorResponse = new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), null);

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
