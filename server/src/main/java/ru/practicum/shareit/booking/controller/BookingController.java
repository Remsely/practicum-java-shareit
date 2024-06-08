package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreationDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private final BookingMapper bookingMapper;

    @PostMapping
    public BookingDto addBooking(@RequestBody BookingCreationDto bookingDto,
                                 @RequestHeader("X-Sharer-User-id") Long userId) {
        log.info("POST /bookings (X-Sharer-User-id = {}). Request body : {}", userId, bookingDto);
        Booking booking = bookingMapper.toEntity(bookingDto);
        return bookingMapper.toDto(bookingService.addBooking(booking, userId));
    }

    @PatchMapping("/{id}")
    public BookingDto approveBooking(@PathVariable long id,
                                     @RequestHeader("X-Sharer-User-id") Long userId,
                                     @RequestParam Boolean approved) {
        log.info("PATCH /bookings/{}?approved={} (X-Sharer-User-id = {})", id, approved, userId);
        return bookingMapper.toDto(bookingService.approveBooking(id, userId, approved));
    }

    @GetMapping("/{id}")
    public BookingDto getBooking(@RequestHeader("X-Sharer-User-id") Long userId, @PathVariable long id) {
        log.info("GET /bookings/{} (X-Sharer-User-id = {})", id, userId);
        return bookingMapper.toDto(bookingService.getBookingById(id, userId));
    }

    @GetMapping
    public List<BookingDto> getUserBookings(@RequestHeader("X-Sharer-User-id") Long userId,
                                            @RequestParam(required = false, defaultValue = "ALL") String state,
                                            @RequestParam(required = false) Integer from,
                                            @RequestParam(required = false) Integer size) {
        log.info("GET /bookings?state={}&from={}&size={} (X-Sharer-User-id = {})", state, from, size, userId);
        return bookingMapper.toDtoList(
                bookingService.getUserBookings(userId, BookingState.valueOf(state), from, size)
        );
    }

    @GetMapping("/owner")
    public List<BookingDto> getUserItemsBookings(@RequestHeader("X-Sharer-User-id") Long userId,
                                                 @RequestParam(required = false, defaultValue = "ALL") String state,
                                                 @RequestParam(required = false) Integer from,
                                                 @RequestParam(required = false) Integer size) {
        log.info("GET /bookings/owner?state={}&from={}&size={} (X-Sharer-User-id = {})", state, from, size, userId);
        return bookingMapper.toDtoList(bookingService.getUserItemsBookings(
                userId, BookingState.valueOf(state), from, size)
        );
    }
}
