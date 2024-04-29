package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.controller.BookingState;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingService {
    Booking addBooking(Booking booking, long userId);

    Booking approveBooking(long bookingId, long userId, boolean approved);

    Booking getBookingById(long bookingId, long userId);

    List<Booking> getUserBookings(long userId, BookingState state);

    List<Booking> getUserItemsBookings(long userId, BookingState state);
}
