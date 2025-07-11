package com.FreeBoard.auth_proxy.exception;

import com.FreeBoard.auth_proxy.exception.ExceptionClass.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;

@ControllerAdvice
@RestControllerAdvice
public class DefaultExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleException(
            ResourceNotFoundException e,
            HttpServletRequest request) {
        ApiError apiError = new ApiError(
                request.getRequestURI(),
                e.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(KeycalokException.class)
    public ResponseEntity<ApiError> handleException(
            KeycalokException e,
            HttpServletRequest request) {
        ApiError apiError = new ApiError(
                request.getRequestURI(),
                e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(SagaFailException.class)
    public ResponseEntity<ApiError> handleException(
            SagaFailException e,
            HttpServletRequest request) {
        ApiError apiError = new ApiError(
                request.getRequestURI(),
                e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(GroupNotFoundException.class)
    public ResponseEntity<ApiError> handleException(
            GroupNotFoundException e,
            HttpServletRequest request) {
        ApiError apiError = new ApiError(
                request.getRequestURI(),
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ApiError> handleException(
            HttpClientErrorException e,
            HttpServletRequest request) {
        ApiError apiError = new ApiError(
                request.getRequestURI(),
                "Authentication error",
                e.getStatusCode().value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiError, e.getStatusCode());
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ApiError> handleException(
            UserAlreadyExistException e,
            HttpServletRequest request) {
        ApiError apiError = new ApiError(
                request.getRequestURI(),
                e.getMessage(),
                HttpStatus.CONFLICT.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AlreadyTakenException.class)
    public ResponseEntity<ApiError> handleException(
            AlreadyTakenException e,
            HttpServletRequest request) {
        ApiError apiError = new ApiError(
                request.getRequestURI(),
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleException(
            MethodArgumentNotValidException e,
            HttpServletRequest request
    ) {

        ApiError apiError = new ApiError(
                request.getRequestURI(),
                "Validation error",
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(InvalidCredential.class)
    public ResponseEntity<ApiError> handleException(
            InvalidCredential e,
            HttpServletRequest request) {
        ApiError apiError = new ApiError(
                request.getRequestURI(),
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleException(
            HttpMessageNotReadableException e,
            HttpServletRequest request) {
        ApiError apiError = new ApiError(
                request.getRequestURI(),
                "Required request body is missing or malformed",
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleException(
            ConstraintViolationException e,
            HttpServletRequest request) {
        ApiError apiError = new ApiError(
                request.getRequestURI(),
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleException(
            MissingServletRequestParameterException e,
            HttpServletRequest request) {
        ApiError apiError = new ApiError(
                request.getRequestURI(),
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleException(
            UserNotFoundException e,
            HttpServletRequest request) {
        ApiError apiError = new ApiError(
                request.getRequestURI(),
                e.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleException(
            BadCredentialsException e,
            HttpServletRequest request
    ) {
        ApiError apiError = new ApiError(
                request.getRequestURI(),
                e.getMessage(),
                HttpStatus.UNAUTHORIZED.value(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
    }

}
