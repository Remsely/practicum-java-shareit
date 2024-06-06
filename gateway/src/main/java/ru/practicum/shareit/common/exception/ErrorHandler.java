package ru.practicum.shareit.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDatesValidation(DatesValidationException e) {
        ErrorResponse errorResponse = e.getErrorResponse();
        log.warn("{} : {}", errorResponse.getReason(), errorResponse.getError());
        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnsupportedState(UnsupportedStateException e) {
        ErrorResponse errorResponse = e.getErrorResponse();
        log.warn("{} : {}", errorResponse.getReason(), errorResponse.getError());
        return errorResponse;
    }
}
