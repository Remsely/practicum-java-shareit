package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.controller.BookingState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingJpaRepository;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.UnavailableItemException;
import ru.practicum.shareit.exception.UserWithoutAccessRightsException;
import ru.practicum.shareit.exception.model.ErrorResponse;
import ru.practicum.shareit.item.repository.ItemJpaRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserJpaRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingJpaRepository bookingRepository;
    private final UserJpaRepository userRepository;
    private final ItemJpaRepository itemRepository;

    @Transactional
    @Override
    public Booking addBooking(Booking booking, long userId) {
        booking.setBooker(userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorResponse.builder()
                                .reason("User repository")
                                .message("User with id " + userId + " does not exist!")
                                .build()
                ))
        );
        long itemId = booking.getItem().getId();
        booking.setItem(itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorResponse.builder()
                                .reason("Item repository")
                                .message("Item with id " + itemId + " does not exist!")
                                .build()
                ))
        );
        if (!booking.getItem().getAvailable()) {
            throw new UnavailableItemException(
                    ErrorResponse.builder()
                            .reason("Unavailable item")
                            .message("The item with id " + itemId + " is not available for booking")
                            .build()
            );
        }
        booking.setStatus(BookingStatus.WAITING);
        Booking savedBooking = bookingRepository.save(booking);
        log.info("add Booking: a booking with an id {} and booker id {} has been added. Booking : {}.",
                savedBooking.getId(), savedBooking.getBooker().getId(), booking.getId());
        return savedBooking;
    }

    @Transactional
    @Override
    public Booking approveBooking(long id, long userId, boolean approved) {
        Booking bookingToUpdate = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorResponse.builder()
                                .reason("Booking repository")
                                .message("Booking with id " + id + " does not exist!").build()
                ));
        if (bookingToUpdate.getItem().getOwner().getId() != userId) {
            throw new UserWithoutAccessRightsException(ErrorResponse.builder()
                    .reason("Forbidden for this id")
                    .message("The user with id " + userId + " does not have access to this booking!")
                    .build());
        }
        bookingToUpdate.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking savedBooking = bookingRepository.save(bookingToUpdate);

        log.info("approve Booking: a booking with an id {} and booker id {} has been {}. Booking : {}.",
                id, userId, approved ? BookingStatus.APPROVED : BookingStatus.REJECTED, savedBooking);
        return savedBooking;
    }

    @Transactional(readOnly = true)
    @Override
    public Booking getBookingById(long bookingId, long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorResponse.builder()
                                .reason("Booking repository")
                                .message("Booking with id " + bookingId + " does not exist!").build()
                ));
        if (booking.getItem().getOwner().getId() != userId && booking.getBooker().getId() != userId) {
            throw new UserWithoutAccessRightsException(ErrorResponse.builder()
                    .reason("Forbidden for this id")
                    .message("The user with id " + userId + " does not have access to this booking!")
                    .build());
        }
        log.info("get Booking: a item booking an id {} has been received. Booking : {}.", booking.getId(), booking);
        return booking;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Booking> getUserBookings(long userId, BookingState state) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorResponse.builder()
                                .reason("User repository")
                                .message("User with id " + userId + " does not exist!")
                                .build()
                ));
        List<Booking> bookings;
        switch (state) {
            case ALL:
                bookings = bookingRepository.findBookingsByBookerOrderByStartDesc(booker);
                break;
            case CURRENT:
                bookings = bookingRepository.findBookingsByBookerAndStartBeforeAndEndAfterOrderByStartDesc(
                        booker, LocalDateTime.now(), LocalDateTime.now());
                break;
            case PAST:
                bookings = bookingRepository.findBookingsByBookerAndEndAfterOrderByStartDesc(
                        booker, LocalDateTime.now());
                break;
            case FUTURE:
                bookings = bookingRepository.findBookingsByBookerAndStartAfterOrderByStartDesc(
                        booker, LocalDateTime.now());
                break;
            case WAITING:
                bookings = bookingRepository.findBookingsByBookerAndStatusOrderByStartDesc(
                        booker, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findBookingsByBookerAndStatusOrderByStartDesc(
                        booker, BookingStatus.REJECTED);
                break;
            default:
                throw new RuntimeException("Unsupported booking state" + state);
        }
        log.info("get Bookings: a bookings with an owner with id {} have been received. Bookings : {}.",
                userId, bookings);
        return bookings;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Booking> getUserItemsBookings(long userId, BookingState state) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorResponse.builder()
                                .reason("User repository")
                                .message("User with id " + userId + " does not exist!")
                                .build()
                ));
        List<Booking> bookings;
        switch (state) {
            case ALL:
                bookings = bookingRepository.findBookingsByItemOwner(owner);
                break;
            case CURRENT:
                bookings = bookingRepository.findCurrentBookingsByItemOwner(owner, LocalDateTime.now());
                break;
            case PAST:
                bookings = bookingRepository.findPastBookingsByItemOwner(owner, LocalDateTime.now());
                break;
            case FUTURE:
                bookings = bookingRepository.findFutureBookingsByItemOwner(owner, LocalDateTime.now());
                break;
            case WAITING:
                bookings = bookingRepository.findBookingsByItemOwnerAndStatus(owner, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findBookingsByItemOwnerAndStatus(owner, BookingStatus.REJECTED);
                break;
            default:
                throw new RuntimeException("Unsupported booking state" + state);
        }
        log.info("get Bookings: a bookings for the user with id {} items have been received. Bookings : {}.",
                userId, bookings);
        return bookings;
    }
}
