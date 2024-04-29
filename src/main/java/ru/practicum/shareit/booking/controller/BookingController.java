package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreationDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.DatesValidationException;
import ru.practicum.shareit.exception.model.ErrorResponse;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private final BookingMapper bookingMapper;

    @PostMapping
    public BookingDto addBooking(@Valid @RequestBody BookingCreationDto bookingDto,
                                 @RequestHeader("X-Sharer-User-id") long userId) {
        log.info("POST /bookings (X-Sharer-User-id = {}). Request body : {}", userId, bookingDto);
        validateDates(bookingDto.getStart(), bookingDto.getEnd());
        Booking booking = bookingMapper.toEntity(bookingDto);
        return bookingMapper.toDto(bookingService.addBooking(booking, userId));
    }

    @PatchMapping("/{id}")
    public BookingDto approveBooking(@PathVariable long id,
                                     @RequestHeader("X-Sharer-User-id") long userId,
                                     @RequestParam Boolean approved) {
        log.info("PATCH /bookings/{}?approved={} (X-Sharer-User-id = {})", id, approved, userId);
        return bookingMapper.toDto(bookingService.approveBooking(id, userId, approved));
    }

    @GetMapping("/{id}")
    public BookingDto getBooking(@RequestHeader("X-Sharer-User-id") long userId, @PathVariable long id) {
        log.info("GET /bookings/{} (X-Sharer-User-id = {})", id, userId);
        return bookingMapper.toDto(bookingService.getBookingById(id, userId));
    }

    @GetMapping
    public List<BookingDto> getCurrentUserBookings(
            @RequestHeader("X-Sharer-User-id") long userId,
            @RequestParam(required = false, defaultValue = "ALL") BookingState state) {
        log.info("GET /bookings?state={} (X-Sharer-User-id = {})", state, userId);
        return bookingMapper.toDtoList(bookingService.getUserBookings(userId, state));
    }

    @GetMapping("/owner")
    public List<BookingDto> getCurrentUserItemsBookings(
            @RequestHeader("X-Sharer-User-id") long userId,
            @RequestParam(required = false, defaultValue = "ALL") BookingState state
    ) {
        log.info("GET /bookings/owner?state={} (X-Sharer-User-id = {})", state, userId);
        return bookingMapper.toDtoList(bookingService.getUserItemsBookings(userId, state));
    }

    private void validateDates(Date from, Date to) {
        if (to.before(from)) {
            throw new DatesValidationException(ErrorResponse.builder()
                    .reason("Booking dates")
                    .message("The end date can't be earlier than the start date.").build());
        }
        if (to.compareTo(from) == 0) {
            throw new DatesValidationException(ErrorResponse.builder()
                    .reason("Booking dates")
                    .message("The end date can't be equal to the start date.").build());
        }
    }
}
