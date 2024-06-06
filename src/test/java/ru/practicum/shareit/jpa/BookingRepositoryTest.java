package ru.practicum.shareit.jpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class BookingRepositoryTest {
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User owner;
    private User booker;
    private Booking booking1;
    private Booking booking2;
    private Booking booking3;
    private Item item;

    @BeforeEach
    public void setUp() {
        owner = User.builder()
                .name("user")
                .email("email@email.ru")
                .build();

        booker = User.builder()
                .name("user2")
                .email("email2@email.ru")
                .build();

        item = Item.builder()
                .available(true)
                .description("description")
                .owner(owner)
                .name("item")
                .build();

        booking1 = Booking.builder()
                .item(item)
                .status(BookingStatus.WAITING)
                .start(LocalDateTime.now().minusDays(7))
                .end(LocalDateTime.now().minusDays(6))
                .booker(booker)
                .build();

        booking2 = Booking.builder()
                .item(item)
                .status(BookingStatus.WAITING)
                .start(LocalDateTime.now().minusDays(4))
                .end(LocalDateTime.now().plusDays(1))
                .booker(booker)
                .build();

        booking3 = Booking.builder()
                .item(item)
                .status(BookingStatus.WAITING)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .booker(booker)
                .build();
    }

    @Test
    public void testFindByBookerOrderByStartDesc() {
        entityManager.persist(owner);
        entityManager.persist(booker);
        entityManager.persist(item);
        entityManager.persist(booking1);
        entityManager.persist(booking2);
        entityManager.persist(booking3);

        Pageable pageable = Pageable.unpaged();

        List<Booking> bookings = bookingRepository.findByBookerOrderByStartDesc(booker, pageable);

        assertThat(bookings).hasSize(3);
        assertThat(bookings.get(0)).isEqualTo(booking3);
        assertThat(bookings.get(1)).isEqualTo(booking2);
        assertThat(bookings.get(2)).isEqualTo(booking1);
    }

    @Test
    public void testFindByBookerAndStartBeforeAndEndAfterOrderByStartDesc() {
        entityManager.persist(owner);
        entityManager.persist(booker);
        entityManager.persist(item);
        entityManager.persist(booking1);
        entityManager.persist(booking2);
        entityManager.persist(booking3);

        Pageable pageable = Pageable.unpaged();

        List<Booking> bookings = bookingRepository.findByBookerAndStartBeforeAndEndAfterOrderByStartDesc(
                booker, LocalDateTime.now().minusDays(1), LocalDateTime.now(), pageable
        );

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0)).isEqualTo(booking2);
    }

    @Test
    public void testFindByBookerAndEndBeforeOrderByStartDesc() {
        entityManager.persist(owner);
        entityManager.persist(booker);
        entityManager.persist(item);
        entityManager.persist(booking1);
        entityManager.persist(booking2);
        entityManager.persist(booking3);

        Pageable pageable = Pageable.unpaged();

        List<Booking> bookings = bookingRepository.findByBookerAndEndBeforeOrderByStartDesc(
                booker, LocalDateTime.now().plusDays(2), pageable
        );

        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0)).isEqualTo(booking2);
        assertThat(bookings.get(1)).isEqualTo(booking1);
    }

    @Test
    public void testFindByBookerAndStartAfterOrderByStartDesc() {
        entityManager.persist(owner);
        entityManager.persist(booker);
        entityManager.persist(item);
        entityManager.persist(booking1);
        entityManager.persist(booking2);
        entityManager.persist(booking3);

        Pageable pageable = Pageable.unpaged();

        List<Booking> bookings = bookingRepository.findByBookerAndStartAfterOrderByStartDesc(
                booker, LocalDateTime.now().minusDays(5), pageable
        );

        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0)).isEqualTo(booking3);
        assertThat(bookings.get(1)).isEqualTo(booking2);
    }

    @Test
    public void testFindByBookerAndStatusOrderByStartDesc() {
        entityManager.persist(owner);
        entityManager.persist(booker);
        entityManager.persist(item);

        booking1.setStatus(BookingStatus.APPROVED);

        entityManager.persist(booking1);
        entityManager.persist(booking2);
        entityManager.persist(booking3);

        Pageable pageable = Pageable.unpaged();

        List<Booking> bookings = bookingRepository.findByBookerAndStatusOrderByStartDesc(
                booker, BookingStatus.WAITING, pageable
        );

        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0)).isEqualTo(booking3);
        assertThat(bookings.get(1)).isEqualTo(booking2);
    }

    @Test
    public void testFindByItemOwnerOrderByStartDesc() {
        entityManager.persist(owner);
        entityManager.persist(booker);
        entityManager.persist(item);
        entityManager.persist(booking1);

        Pageable pageable = Pageable.unpaged();

        List<Booking> bookings = bookingRepository.findByItemOwnerOrderByStartDesc(owner, pageable);

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0)).isEqualTo(booking1);
    }

    @Test
    public void testFindCurrentByItemOwnerOrderByStartDesc() {
        entityManager.persist(owner);
        entityManager.persist(booker);
        entityManager.persist(item);
        entityManager.persist(booking1);
        entityManager.persist(booking2);
        entityManager.persist(booking3);

        Pageable pageable = Pageable.unpaged();

        List<Booking> bookings = bookingRepository.findCurrentByItemOwnerOrderByStartDesc(
                owner, LocalDateTime.now(), pageable
        );

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0)).isEqualTo(booking2);
    }

    @Test
    public void testFindPastByItemOwnerOrderByStartDesc() {
        entityManager.persist(owner);
        entityManager.persist(booker);
        entityManager.persist(item);
        entityManager.persist(booking1);
        entityManager.persist(booking2);
        entityManager.persist(booking3);

        Pageable pageable = Pageable.unpaged();

        List<Booking> bookings = bookingRepository.findPastByItemOwnerOrderByStartDesc(
                owner, LocalDateTime.now(), pageable
        );

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0)).isEqualTo(booking1);
    }

    @Test
    public void testFindFutureByItemOwnerOrderByStartDesc() {
        entityManager.persist(owner);
        entityManager.persist(booker);
        entityManager.persist(item);
        entityManager.persist(booking1);
        entityManager.persist(booking2);
        entityManager.persist(booking3);

        Pageable pageable = Pageable.unpaged();

        List<Booking> bookings = bookingRepository.findFutureByItemOwnerOrderByStartDesc(
                owner, LocalDateTime.now(), pageable
        );

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0)).isEqualTo(booking3);
    }

    @Test
    public void findByItemOwnerAndStatusOrderByStartDesc() {
        entityManager.persist(owner);
        entityManager.persist(booker);
        entityManager.persist(item);

        booking1.setStatus(BookingStatus.APPROVED);

        entityManager.persist(booking1);
        entityManager.persist(booking2);
        entityManager.persist(booking3);

        Pageable pageable = Pageable.unpaged();

        List<Booking> bookings = bookingRepository.findByItemOwnerAndStatusOrderByStartDesc(
                owner, BookingStatus.WAITING, pageable
        );

        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0)).isEqualTo(booking3);
        assertThat(bookings.get(1)).isEqualTo(booking2);
    }

    @Test
    public void testFindByItemInAndStatusOrderByItem() {
        entityManager.persist(owner);
        entityManager.persist(booker);
        entityManager.persist(item);

        Item item2 = Item.builder()
                .description("description")
                .name("name")
                .available(true)
                .build();

        entityManager.persist(item2);

        booking1.setStatus(BookingStatus.APPROVED);
        booking2.setItem(item2);

        entityManager.persist(booking1);
        entityManager.persist(booking2);
        entityManager.persist(booking3);

        List<Booking> bookings = bookingRepository.findByItemInAndStatusOrderByItem(
                List.of(item, item2), BookingStatus.WAITING
        );

        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0)).isEqualTo(booking3);
        assertThat(bookings.get(1)).isEqualTo(booking2);
    }

    @Test
    public void testFindByItemAndStatus() {
        entityManager.persist(owner);
        entityManager.persist(booker);
        entityManager.persist(item);

        booking1.setStatus(BookingStatus.APPROVED);

        entityManager.persist(booking1);
        entityManager.persist(booking2);
        entityManager.persist(booking3);

        List<Booking> bookings = bookingRepository.findByItemAndStatus(
                item, BookingStatus.WAITING
        );

        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0)).isEqualTo(booking2);
        assertThat(bookings.get(1)).isEqualTo(booking3);
    }

    @Test
    public void testExistByItemAndBookerAndStatusAndEndBefore() {
        entityManager.persist(owner);
        entityManager.persist(booker);
        entityManager.persist(item);

        booking1.setStatus(BookingStatus.APPROVED);

        entityManager.persist(booking1);
        entityManager.persist(booking2);
        entityManager.persist(booking3);

        boolean exists = bookingRepository.existsByItemAndBookerAndStatusAndEndBefore(
                item, booker, BookingStatus.WAITING, LocalDateTime.now()
        );

        assertThat(exists).isEqualTo(false);

        exists = bookingRepository.existsByItemAndBookerAndStatusAndEndBefore(
                item, booker, BookingStatus.APPROVED, LocalDateTime.now()
        );

        assertThat(exists).isEqualTo(true);
    }
}
