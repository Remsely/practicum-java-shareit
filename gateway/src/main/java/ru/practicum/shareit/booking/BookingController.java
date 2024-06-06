package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.common.exception.DatesValidationException;
import ru.practicum.shareit.common.exception.ErrorResponse;
import ru.practicum.shareit.common.exception.UnsupportedStateException;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingClient client;

    @PostMapping
    public ResponseEntity<?> postBooking(@Valid @RequestBody BookingCreationDto bookingDto,
                                         @RequestHeader("X-Sharer-User-id") Long userId) {
        log.info("POST /bookings (X-Sharer-User-id = {}). Request body : {}", userId, bookingDto);
        validateDates(bookingDto.getStart(), bookingDto.getEnd());
        return client.postBooking(bookingDto, userId);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> approveBooking(@PathVariable long id,
                                            @RequestHeader("X-Sharer-User-id") Long userId,
                                            @RequestParam Boolean approved) {
        log.info("PATCH /bookings/{}?approved={} (X-Sharer-User-id = {})", id, approved, userId);
        return client.approveBooking(id, userId, approved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBooking(@RequestHeader("X-Sharer-User-id") Long userId, @PathVariable long id) {
        log.info("GET /bookings/{} (X-Sharer-User-id = {})", id, userId);
        return client.getBooking(id, userId);
    }

    @GetMapping
    public ResponseEntity<?> getUserBookings(@RequestHeader("X-Sharer-User-id") Long userId,
                                             @RequestParam(required = false, defaultValue = "ALL") String state,
                                             @RequestParam(required = false) Integer from,
                                             @RequestParam(required = false) Integer size) {
        log.info("GET /bookings?state={}&from={}&size={} (X-Sharer-User-id = {})", state, from, size, userId);
        BookingState bookingState = validateBookingState(state);
        return client.getUserBookings(userId, bookingState, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<?> getUserItemsBookings(@RequestHeader("X-Sharer-User-id") Long userId,
                                                  @RequestParam(required = false, defaultValue = "ALL") String state,
                                                  @RequestParam(required = false) Integer from,
                                                  @RequestParam(required = false) Integer size) {
        log.info("GET /bookings/owner?state={}&from={}&size={} (X-Sharer-User-id = {})", state, from, size, userId);
        BookingState bookingState = validateBookingState(state);
        return client.getUserItemsBookings(userId, bookingState, from, size);
    }

    private void validateDates(LocalDateTime from, LocalDateTime to) {
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

    private BookingState validateBookingState(String state) {
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
