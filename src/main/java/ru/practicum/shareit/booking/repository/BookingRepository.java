package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @EntityGraph(attributePaths = {"booker", "item"})
    List<Booking> findByBookerOrderByStartDesc(User booker, Pageable pageable);

    @EntityGraph(attributePaths = {"booker", "item"})
    List<Booking> findByBookerAndStartBeforeAndEndAfterOrderByStartDesc(
            User booker, LocalDateTime start, LocalDateTime end, Pageable pageable);

    @EntityGraph(attributePaths = {"booker", "item"})
    List<Booking> findByBookerAndEndBeforeOrderByStartDesc(User booker, LocalDateTime end, Pageable pageable);

    @EntityGraph(attributePaths = {"booker", "item"})
    List<Booking> findByBookerAndStartAfterOrderByStartDesc(User booker, LocalDateTime start, Pageable pageable);

    @EntityGraph(attributePaths = {"booker", "item"})
    List<Booking> findByBookerAndStatusOrderByStartDesc(User booker, BookingStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"booker", "item"})
    @Query(" select b from Booking b " +
            "where b.item.owner = ?1 " +
            "order by b.start desc")
    List<Booking> findByItemOwnerOrderByStartDesc(User owner, Pageable pageable);

    @EntityGraph(attributePaths = {"booker", "item"})
    @Query(" select b from Booking b " +
            "where b.item.owner = :owner and b.start <= :now and b.end >= :now " +
            "order by b.start desc")
    List<Booking> findCurrentByItemOwnerOrderByStartDesc(User owner, LocalDateTime now, Pageable pageable);

    @EntityGraph(attributePaths = {"booker", "item"})
    @Query(" select b from Booking b " +
            "where b.item.owner = :owner and b.end < :now " +
            "order by b.start desc")
    List<Booking> findPastByItemOwnerOrderByStartDesc(User owner, LocalDateTime now, Pageable pageable);

    @EntityGraph(attributePaths = {"booker", "item"})
    @Query(" select b from Booking b " +
            "where b.item.owner = :owner and b.start > :now " +
            "order by b.start desc")
    List<Booking> findFutureByItemOwnerOrderByStartDesc(User owner, LocalDateTime now, Pageable pageable);

    @EntityGraph(attributePaths = {"booker", "item"})
    @Query(" select b from Booking b " +
            "where b.item.owner = :owner and b.status = :status " +
            "order by b.start desc")
    List<Booking> findByItemOwnerAndStatusOrderByStartDesc(User owner, BookingStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"booker", "item"})
    List<Booking> findByItemInAndStatusOrderByItem(List<Item> items, BookingStatus status);

    @EntityGraph(attributePaths = {"booker", "item"})
    List<Booking> findByItemAndStatus(Item item, BookingStatus status);

    boolean existsByItemAndBookerAndStatusAndEndBefore(
            Item item, User booker, BookingStatus status, LocalDateTime end);
}
