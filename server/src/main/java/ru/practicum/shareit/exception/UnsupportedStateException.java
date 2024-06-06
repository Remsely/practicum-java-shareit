package ru.practicum.shareit.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.exception.model.ErrorResponse;

@Getter
@RequiredArgsConstructor
public class UnsupportedStateException extends IllegalArgumentException {
    private final ErrorResponse errorResponse;
}
