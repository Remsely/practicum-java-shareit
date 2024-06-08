package ru.practicum.shareit.common.exception;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {
    private final String reason;
    private final String error;
}
