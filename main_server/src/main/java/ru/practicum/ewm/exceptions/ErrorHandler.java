package ru.practicum.ewm.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static ru.practicum.ewm.exceptions.ErrorCode.BAD_REQUEST;
import static ru.practicum.ewm.exceptions.ErrorCode.CONFLICT;
import static ru.practicum.ewm.exceptions.ErrorCode.NOT_FOUND;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @Data
    @Builder
    @AllArgsConstructor
    private static class ErrorResponse {
        private String status;
        private String reason;
        private String message;
        private String timestamp;
    }

    private static final String DT_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern(DT_FORMAT);

    private final ObjectMapper mapper = new ObjectMapper();

    @ExceptionHandler({
            ValidationException.class,
            MethodArgumentNotValidException.class,
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidationException(final Throwable exc) {
        log.error(exc.getClass().getSimpleName(), exc.getMessage());

        final ErrorResponse response = prepareResponse(BAD_REQUEST, "Incorrectly made request.", exc);
        return safeResponse(response);
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFoundException(final NotFoundException exc) {
        log.error(exc.getClass().getSimpleName(), exc.getMessage());

        final ErrorResponse response = prepareResponse(NOT_FOUND, "The required object was not found.", exc);
        return safeResponse(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDataIntegrityViolationException(final DataIntegrityViolationException exc) {
        log.error(exc.getClass().getSimpleName(), exc.getMessage());

        final ErrorResponse response = prepareResponse(CONFLICT, "Integrity constraint has been violated.", exc);
        return safeResponse(response);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(final Throwable exc) {
        log.error(exc.getClass().getSimpleName(), exc.getMessage());
        return ResponseEntity.internalServerError().toString();
    }

    private String safeResponse(final ErrorResponse response) {
        try {
            return mapper.writeValueAsString(response);
        } catch (final JsonProcessingException ex) {
            return ResponseEntity.internalServerError().toString();
        }
    }

    private ErrorResponse prepareResponse(
            final ErrorCode code,
            final String reason,
            final Throwable exc) {
        final String timestamp = LocalDateTime.now().format(DT_FORMATTER);
        return ErrorResponse.builder()
                .status(code.name())
                .reason(reason)
                .message(exc.getMessage())
                .timestamp(timestamp)
                .build();
    }
}
