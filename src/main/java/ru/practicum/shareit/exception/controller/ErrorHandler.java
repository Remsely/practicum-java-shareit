package ru.practicum.shareit.exception.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.exception.IllegalStateException;
import ru.practicum.shareit.exception.model.ErrorResponse;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleEntityNotFound(EntityNotFoundException e) {
        ErrorResponse errorResponse = e.getErrorResponse();
        log.warn("{} : {}", errorResponse.getReason(), errorResponse.getError());
        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleUserWithSuchEmailExist(UserAlreadyExistException e) {
        ErrorResponse errorResponse = e.getErrorResponse();
        log.warn("{} : {}", errorResponse.getReason(), errorResponse.getError());
        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleUserWithoutAccess(UserWithoutAccessRightsException e) {
        ErrorResponse errorResponse = e.getErrorResponse();
        log.warn("{} : {}", errorResponse.getReason(), errorResponse.getError());
        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDatesValidation(DatesValidationException e) {
        ErrorResponse errorResponse = e.getErrorResponse();
        log.warn("{} : {}", errorResponse.getReason(), errorResponse.getError());
        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnavailableItem(UnavailableItemException e) {
        ErrorResponse errorResponse = e.getErrorResponse();
        log.warn("{} : {}", errorResponse.getReason(), errorResponse.getError());
        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleIllegalState(IllegalStateException e) {
        ErrorResponse errorResponse = e.getErrorResponse();
        log.warn("{} : {}", errorResponse.getReason(), errorResponse.getError());
        return errorResponse;
    }
}
