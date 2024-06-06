package ru.practicum.shareit.integration.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.controller.BookingState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceTest {
    private final BookingService bookingService;
    private final UserService userService;
    private final ItemService itemService;
    private User owner;
    private User booker;
    private Item item;
    private Booking bookingStartMinus7EndMinus5;
    private Booking bookingStartMinus1EndPlus1;
    private Booking bookingStartPlus1EndPlus2;
    private Booking bookingStartMinus2EndPlus2;
    private Booking bookingStartMinus8EndMinus7;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .name("user")
                .email("email@email.com")
                .build();

        booker = User.builder()
                .name("user2")
                .email("email2@email.com")
                .build();

        item = Item.builder()
                .owner(owner)
                .name("item")
                .description("item description")
                .available(true)
                .build();

        bookingStartMinus7EndMinus5 = Booking.builder()
                .item(item)
                .start(LocalDateTime.now().minusDays(7))
                .end(LocalDateTime.now().minusDays(5))
                .build();

        bookingStartMinus1EndPlus1 = Booking.builder()
                .item(item)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        bookingStartPlus1EndPlus2 = Booking.builder()
                .item(item)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        bookingStartMinus2EndPlus2 = Booking.builder()
                .item(item)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        bookingStartMinus8EndMinus7 = Booking.builder()
                .item(item)
                .start(LocalDateTime.now().minusDays(8))
                .end(LocalDateTime.now().minusDays(7))
                .build();
    }

    @Test
    public void testGetUserBookings() {
        assertThrows(EntityNotFoundException.class, () ->
                bookingService.getUserBookings(2L, BookingState.ALL, 0, 1));

        User owner = userService.addUser(this.owner);
        User booker = userService.addUser(this.booker);
        itemService.addItem(this.item, owner.getId());

        Booking bookingStartMinus7EndMinus5 = bookingService.addBooking(
                this.bookingStartMinus7EndMinus5, booker.getId());
        Booking bookingStartMinus1EndPlus1 = bookingService.addBooking(
                this.bookingStartMinus1EndPlus1, booker.getId());
        Booking bookingStartPlus1EndPlus2 = bookingService.addBooking(
                this.bookingStartPlus1EndPlus2, booker.getId());
        Booking bookingStartMinus2EndPlus2 = bookingService.addBooking(
                this.bookingStartMinus2EndPlus2, booker.getId());
        Booking bookingStartMinus8EndMinus7 = bookingService.addBooking(
                this.bookingStartMinus8EndMinus7, booker.getId());

        List<Booking> bookings = bookingService.getUserBookings(booker.getId(), BookingState.ALL, null, null);

        assertThat(bookings.size()).isEqualTo(5);
        assertThat(bookings.get(0)).isEqualTo(bookingStartPlus1EndPlus2);
        assertThat(bookings.get(1)).isEqualTo(bookingStartMinus1EndPlus1);
        assertThat(bookings.get(2)).isEqualTo(bookingStartMinus2EndPlus2);
        assertThat(bookings.get(3)).isEqualTo(bookingStartMinus7EndMinus5);
        assertThat(bookings.get(4)).isEqualTo(bookingStartMinus8EndMinus7);

        bookings = bookingService.getUserBookings(booker.getId(), BookingState.CURRENT, null, null);

        assertThat(bookings.size()).isEqualTo(2);
        assertThat(bookings.get(0)).isEqualTo(bookingStartMinus1EndPlus1);
        assertThat(bookings.get(1)).isEqualTo(bookingStartMinus2EndPlus2);

        bookings = bookingService.getUserBookings(booker.getId(), BookingState.PAST, null, null);

        assertThat(bookings.size()).isEqualTo(2);
        assertThat(bookings.get(0)).isEqualTo(bookingStartMinus7EndMinus5);
        assertThat(bookings.get(1)).isEqualTo(bookingStartMinus8EndMinus7);

        bookings = bookingService.getUserBookings(booker.getId(), BookingState.FUTURE, null, null);

        assertThat(bookings.size()).isEqualTo(1);
        assertThat(bookings.get(0)).isEqualTo(bookingStartPlus1EndPlus2);

        bookingStartMinus1EndPlus1 = bookingService.approveBooking(bookingStartMinus1EndPlus1.getId(), owner.getId(), false);
        bookingStartPlus1EndPlus2 = bookingService.approveBooking(bookingStartPlus1EndPlus2.getId(), owner.getId(), false);

        bookings = bookingService.getUserBookings(booker.getId(), BookingState.WAITING, null, null);

        assertThat(bookings.size()).isEqualTo(3);
        assertThat(bookings.get(0)).isEqualTo(bookingStartMinus2EndPlus2);
        assertThat(bookings.get(0).getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(bookings.get(1)).isEqualTo(bookingStartMinus7EndMinus5);
        assertThat(bookings.get(1).getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(bookings.get(2)).isEqualTo(bookingStartMinus8EndMinus7);
        assertThat(bookings.get(2).getStatus()).isEqualTo(BookingStatus.WAITING);

        bookingService.approveBooking(bookingStartMinus7EndMinus5.getId(), owner.getId(), true);

        bookings = bookingService.getUserBookings(booker.getId(), BookingState.REJECTED, null, null);

        assertThat(bookings.size()).isEqualTo(2);
        assertThat(bookings.get(0)).isEqualTo(bookingStartPlus1EndPlus2);
        assertThat(bookings.get(0).getStatus()).isEqualTo(BookingStatus.REJECTED);
        assertThat(bookings.get(1)).isEqualTo(bookingStartMinus1EndPlus1);
        assertThat(bookings.get(1).getStatus()).isEqualTo(BookingStatus.REJECTED);
    }
}
