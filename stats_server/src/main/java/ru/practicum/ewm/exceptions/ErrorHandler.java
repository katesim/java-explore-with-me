package ru.practicum.ewm.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @Data
    @AllArgsConstructor
    private static class ErrorResponse {
        private String code;
        private String message;
    }

    private static final String VALIDATION_ERROR_CODE = "VALIDATION_ERROR";

    private final ObjectMapper mapper = new ObjectMapper();

    @ExceptionHandler({
            ValidationException.class,
            ConstraintViolationException.class,
            MethodArgumentNotValidException.class,
            IllegalArgumentException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidationException(final Throwable exception) {
        log.error(exception.getClass().getSimpleName(), exception.getMessage());
        final ErrorResponse response = new ErrorResponse(VALIDATION_ERROR_CODE, exception.getMessage());

        try {
            return mapper.writeValueAsString(response);
        } catch (final JsonProcessingException exc) {
            return ResponseEntity.internalServerError().toString();
        }
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Throwable exception) {
        log.error(exception.getClass().getSimpleName(), exception.getMessage());
        return ResponseEntity.internalServerError().toString();
    }
}
