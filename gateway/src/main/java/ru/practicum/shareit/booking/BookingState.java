package ru.practicum.shareit.booking;

import java.util.Optional;

public enum BookingState {
    ALL, CURRENT, FUTURE, WAITING, PAST, REJECTED;

    public static Optional<BookingState> fromString(String state) {
        try {
            return Optional.of(BookingState.valueOf(state.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
