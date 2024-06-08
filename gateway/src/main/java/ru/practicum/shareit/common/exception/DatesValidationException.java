package ru.practicum.shareit.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DatesValidationException extends RuntimeException {
    private final ErrorResponse errorResponse;
}
