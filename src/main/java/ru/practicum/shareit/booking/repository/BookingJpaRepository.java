package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface BookingJpaRepository extends JpaRepository<Booking, Long> {
    @EntityGraph(attributePaths = {"booker"})
    List<Booking> findBookingsByBookerOrderByStartDesc(User booker);

    @EntityGraph(attributePaths = {"booker"})
    List<Booking> findBookingsByBookerAndStartBeforeAndEndAfterAndStatusOrderByStartDesc(
            User booker, LocalDateTime start, LocalDateTime end, BookingStatus status);

    @EntityGraph(attributePaths = {"booker"})
    List<Booking> findBookingsByBookerAndEndAfterAndStatusOrderByStartDesc(User booker, LocalDateTime end, BookingStatus status);

    @EntityGraph(attributePaths = {"booker"})
    List<Booking> findBookingsByBookerAndStartBeforeAndStatusOrderByStartDesc(
            User booker, LocalDateTime start, BookingStatus status);

    @EntityGraph(attributePaths = {"booker"})
    List<Booking> findBookingsByBookerAndStatusOrderByStartDesc(User booker, BookingStatus status);

    @EntityGraph(attributePaths = {"booker"})
    @Query(" select b from Booking b " +
            "where b.item.owner = ?1 " +
            "order by b.start desc")
    List<Booking> findBookingsByItemOwner(User owner);

    @EntityGraph(attributePaths = {"booker"})
    @Query(" select b from Booking b " +
            "where b.item.owner = :owner and b.start <= :now and b.end >= :now and b.status = :status " +
            "order by b.start desc")
    List<Booking> findCurrentBookingsByItemOwnerAndStatus(User owner, LocalDateTime now, BookingStatus status);

    @EntityGraph(attributePaths = {"booker"})
    @Query(" select b from Booking b " +
            "where b.item.owner = :owner and b.end < :now and b.status = :status " +
            "order by b.start desc")
    List<Booking> findPastBookingsByItemOwnerAndStatus(User owner, LocalDateTime now, BookingStatus status);

    @EntityGraph(attributePaths = {"booker"})
    @Query(" select b from Booking b " +
            "where b.item.owner = :owner and b.start > :now and b.status = :status " +
            "order by b.start desc")
    List<Booking> findFutureBookingsByItemOwnerAndStatus(User owner, LocalDateTime now, BookingStatus status);

    @EntityGraph(attributePaths = {"booker"})
    @Query(" select b from Booking b " +
            "where b.item.owner = :owner and b.status = :status " +
            "order by b.start desc")
    List<Booking> findBookingsByItemOwnerAndStatus(User owner, BookingStatus status);
}
