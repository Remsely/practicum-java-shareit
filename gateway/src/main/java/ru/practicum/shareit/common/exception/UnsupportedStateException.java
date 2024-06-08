package ru.practicum.shareit.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UnsupportedStateException extends IllegalArgumentException {
    private final ErrorResponse errorResponse;
}
