package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.controller.BookingState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingJpaRepository;
import ru.practicum.shareit.common.utils.PageableUtility;
import ru.practicum.shareit.exception.AlreadyApprovedException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.UnavailableItemException;
import ru.practicum.shareit.exception.UserWithoutAccessRightsException;
import ru.practicum.shareit.exception.model.ErrorResponse;
import ru.practicum.shareit.item.model.Item;
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
    private final PageableUtility pageableUtility;

    @Transactional
    @Override
    public Booking addBooking(Booking booking, long userId) {
        booking.setBooker(findUser(userId));

        long itemId = booking.getItem().getId();
        booking.setItem(findItem(itemId));

        checkBookerIsNotOwner(booking, userId);
        checkBookingItemIsAvailable(booking);

        booking.setStatus(BookingStatus.WAITING);
        Booking savedBooking = bookingRepository.save(booking);
        log.info("add Booking: a booking with an id {} and booker id {} has been added. Booking : {}.",
                savedBooking.getId(), savedBooking.getBooker().getId(), booking.getId());
        return savedBooking;
    }

    @Override
    public Booking approveBooking(long id, long userId, boolean approved) {
        Booking bookingToUpdate = findBooking(id);

        checkOwner(bookingToUpdate, userId);
        checkBookingIsWaiting(bookingToUpdate);

        bookingToUpdate.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        Booking savedBooking = bookingRepository.save(bookingToUpdate);
        log.info("approve Booking: a booking with an id {} and booker id {} has been {}. Booking : {}.",
                id, userId, approved ? BookingStatus.APPROVED : BookingStatus.REJECTED, savedBooking);
        return savedBooking;
    }

    @Override
    public Booking getBookingById(long bookingId, long userId) {
        Booking booking = findBooking(bookingId);
        checkUserAccessRights(booking, userId);
        log.info("get Booking: a item booking an id {} has been received. Booking : {}.", booking.getId(), booking);
        return booking;
    }

    @Override
    public List<Booking> getUserBookings(long userId, BookingState state, Integer from, Integer size) {
        User booker = findUser(userId);
        Pageable pageable = pageableUtility.getPageableFromArguments(from, size);
        List<Booking> bookings;
        switch (state) {
            case ALL:
                bookings = bookingRepository.findBookingsByBookerOrderByStartDesc(booker, pageable);
                break;
            case CURRENT:
                bookings = bookingRepository.findBookingsByBookerAndStartBeforeAndEndAfterOrderByStartDesc(
                        booker, LocalDateTime.now(), LocalDateTime.now(), pageable);
                break;
            case PAST:
                bookings = bookingRepository.findBookingsByBookerAndEndBeforeOrderByStartDesc(
                        booker, LocalDateTime.now(), pageable);
                break;
            case FUTURE:
                bookings = bookingRepository.findBookingsByBookerAndStartAfterOrderByStartDesc(
                        booker, LocalDateTime.now(), pageable);
                break;
            case WAITING:
                bookings = bookingRepository.findBookingsByBookerAndStatusOrderByStartDesc(
                        booker, BookingStatus.WAITING, pageable);
                break;
            case REJECTED:
                bookings = bookingRepository.findBookingsByBookerAndStatusOrderByStartDesc(
                        booker, BookingStatus.REJECTED, pageable);
                break;
            default:
                throw new RuntimeException("Unsupported booking state" + state);
        }
        log.info("get Bookings: a bookings with an owner with id {} have been received. List (size = {}) : {}.",
                userId, bookings.size(), bookings);
        return bookings;
    }

    @Override
    public List<Booking> getUserItemsBookings(long userId, BookingState state, Integer from, Integer size) {
        User owner = findUser(userId);
        Pageable pageable = pageableUtility.getPageableFromArguments(from, size);
        List<Booking> bookings;
        switch (state) {
            case ALL:
                bookings = bookingRepository.findBookingsByItemOwner(owner, pageable);
                break;
            case CURRENT:
                bookings = bookingRepository.findCurrentBookingsByItemOwner(owner, LocalDateTime.now(), pageable);
                break;
            case PAST:
                bookings = bookingRepository.findPastBookingsByItemOwner(owner, LocalDateTime.now(), pageable);
                break;
            case FUTURE:
                bookings = bookingRepository.findFutureBookingsByItemOwner(owner, LocalDateTime.now(), pageable);
                break;
            case WAITING:
                bookings = bookingRepository.findBookingsByItemOwnerAndStatus(owner, BookingStatus.WAITING, pageable);
                break;
            case REJECTED:
                bookings = bookingRepository.findBookingsByItemOwnerAndStatus(owner, BookingStatus.REJECTED, pageable);
                break;
            default:
                throw new RuntimeException("Unsupported booking state" + state);
        }
        log.info("get Bookings: a bookings for the user with id {} items have been received. List (size = {}) : {}.",
                userId, bookings.size(), bookings);
        return bookings;
    }

    private User findUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorResponse.builder()
                                .reason("User repository")
                                .error("User with id " + userId + " does not exist!")
                                .build()
                ));
    }

    private Booking findBooking(long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorResponse.builder()
                                .reason("Booking repository")
                                .error("Booking with id " + bookingId + " does not exist!").build()
                ));
    }

    private Item findItem(long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorResponse.builder()
                                .reason("Item repository")
                                .error("Item with id " + itemId + " does not exist!")
                                .build()
                ));
    }

    private boolean userIsItemOwner(Booking booking, long userId) {
        return booking.getItem().getOwner().getId() == userId;
    }

    private boolean userIsBooker(Booking booking, long userId) {
        return booking.getBooker().getId() == userId;
    }

    private void checkBookerIsNotOwner(Booking booking, long userId) {
        if (userIsItemOwner(booking, userId)) {
            throw new UserWithoutAccessRightsException(ErrorResponse.builder()
                    .reason("Forbidden for this id")
                    .error("The user with id " + userId + " does not have access to this booking!")
                    .build()
            );
        }
    }

    private void checkOwner(Booking booking, long userId) {
        if (!userIsItemOwner(booking, userId)) {
            throw new UserWithoutAccessRightsException(ErrorResponse.builder()
                    .reason("Forbidden for this id")
                    .error("The user with id " + userId + " does not have access to this booking!")
                    .build()
            );
        }
    }

    private void checkBookingItemIsAvailable(Booking booking) {
        Item item = booking.getItem();
        if (!item.getAvailable()) {
            throw new UnavailableItemException(
                    ErrorResponse.builder()
                            .reason("Unavailable item")
                            .error("The item with id " + item.getId() + " is not available for booking!")
                            .build()
            );
        }
    }

    private void checkBookingIsWaiting(Booking booking) {
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new AlreadyApprovedException(ErrorResponse.builder()
                    .reason("Booking status")
                    .error("The booking request has already been approved!")
                    .build()
            );
        }
    }

    private void checkUserAccessRights(Booking booking, long userId) {
        if (!userIsItemOwner(booking, userId) && !userIsBooker(booking, userId)) {
            throw new UserWithoutAccessRightsException(ErrorResponse.builder()
                    .reason("Forbidden for this id")
                    .error("The user with id " + userId + " does not have access to this booking!")
                    .build()
            );
        }
    }
}
