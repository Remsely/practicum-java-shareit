package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingJpaRepository extends JpaRepository<Booking, Long> {
    @EntityGraph(attributePaths = {"booker"})
    List<Booking> findBookingsByBookerOrderByStartDesc(User booker);

    @EntityGraph(attributePaths = {"booker"})
    List<Booking> findBookingsByBookerAndStartBeforeAndEndAfterOrderByStartDesc(
            User booker, LocalDateTime start, LocalDateTime end);

    @EntityGraph(attributePaths = {"booker"})
    List<Booking> findBookingsByBookerAndEndAfterOrderByStartDesc(User booker, LocalDateTime end);

    @EntityGraph(attributePaths = {"booker"})
    List<Booking> findBookingsByBookerAndStartAfterOrderByStartDesc(User booker, LocalDateTime start);

    @EntityGraph(attributePaths = {"booker"})
    List<Booking> findBookingsByBookerAndStatusOrderByStartDesc(User booker, BookingStatus status);

    @EntityGraph(attributePaths = {"booker"})
    @Query(" select b from Booking b " +
            "where b.item.owner = ?1 " +
            "order by b.start desc")
    List<Booking> findBookingsByItemOwner(User owner);

    @EntityGraph(attributePaths = {"booker"})
    @Query(" select b from Booking b " +
            "where b.item.owner = :owner and b.start <= :now and b.end >= :now " +
            "order by b.start desc")
    List<Booking> findCurrentBookingsByItemOwner(User owner, LocalDateTime now);

    @EntityGraph(attributePaths = {"booker"})
    @Query(" select b from Booking b " +
            "where b.item.owner = :owner and b.end < :now " +
            "order by b.start desc")
    List<Booking> findPastBookingsByItemOwner(User owner, LocalDateTime now);

    @EntityGraph(attributePaths = {"booker"})
    @Query(" select b from Booking b " +
            "where b.item.owner = :owner and b.start > :now " +
            "order by b.start desc")
    List<Booking> findFutureBookingsByItemOwner(User owner, LocalDateTime now);

    @EntityGraph(attributePaths = {"booker"})
    @Query(" select b from Booking b " +
            "where b.item.owner = :owner and b.status = :status " +
            "order by b.start desc")
    List<Booking> findBookingsByItemOwnerAndStatus(User owner, BookingStatus status);
}
