package ru.practicum.shareit.booking.controller;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.common.exception.DatesValidationException;
import ru.practicum.shareit.common.exception.ErrorResponse;
import ru.practicum.shareit.common.exception.UnsupportedStateException;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class BookingValidator {
    public void validateDates(LocalDateTime from, LocalDateTime to) {
        if (to.isBefore(from)) {
            throw new DatesValidationException(ErrorResponse.builder()
                    .reason("Booking dates")
                    .error("The end date can't be earlier than the start date!")
                    .build()
            );
        }
        if (to.isEqual(from)) {
            throw new DatesValidationException(ErrorResponse.builder()
                    .reason("Booking dates")
                    .error("The end date can't be equal to the start date!")
                    .build()
            );
        }
    }

    public BookingState validateBookingState(String state) {
        Optional<BookingState> stateOptional = BookingState.fromString(state);
        if (stateOptional.isPresent()) {
            return stateOptional.get();
        }
        throw new UnsupportedStateException(ErrorResponse.builder()
                .reason("State parameter")
                .error("Unknown state: " + state)
                .build()
        );
    }
}
