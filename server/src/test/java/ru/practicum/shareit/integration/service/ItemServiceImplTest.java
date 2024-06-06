package ru.practicum.shareit.integration.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.dto.ItemExtraInfoDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
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
public class ItemServiceImplTest {
    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;
    private final ItemMapper itemMapper;
    private User user1;
    private User user2;
    private Item item1ByUser1;
    private Item item2ByUser1;
    private Item item3ByUser1;
    private Item item1ByUser2;
    private Item item2ByUser2;
    private Booking bookingLastToItem1ByUser1;
    private Booking bookingNextToItem1ByUser1;
    private Booking bookingLastToItem2ByUser2;
    private Booking bookingNextToItem1ByUser2;
    private Comment comment1ToItem1ByUser1;
    private Comment comment2ToItem1ByUser1;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .name("user")
                .email("email@email.com")
                .build();

        user2 = User.builder()
                .name("user2")
                .email("email2@email.com")
                .build();

        item1ByUser1 = Item.builder()
                .available(true)
                .name("item1 user1")
                .description("description1 user1")
                .build();

        item2ByUser1 = Item.builder()
                .available(true)
                .name("item2 user1")
                .description("description2 user1")
                .build();

        item3ByUser1 = Item.builder()
                .available(true)
                .name("item3 user1")
                .description("description3 user1")
                .build();

        item1ByUser2 = Item.builder()
                .available(true)
                .name("item1 user2")
                .description("description1 user2")
                .build();

        item2ByUser2 = Item.builder()
                .available(true)
                .name("item2 user2")
                .description("description2 user2")
                .build();

        bookingLastToItem1ByUser1 = Booking.builder()
                .item(item1ByUser1)
                .start(LocalDateTime.now().minusDays(7))
                .end(LocalDateTime.now().minusDays(5))
                .build();

        bookingNextToItem1ByUser1 = Booking.builder()
                .item(item1ByUser1)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(4))
                .build();

        bookingLastToItem2ByUser2 = Booking.builder()
                .item(item2ByUser2)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        bookingNextToItem1ByUser2 = Booking.builder()
                .item(item1ByUser2)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(4))
                .build();

        comment1ToItem1ByUser1 = Comment.builder()
                .text("comment1 item1 user1")
                .build();

        comment2ToItem1ByUser1 = Comment.builder()
                .text("comment2 item1 user1")
                .build();
    }

    @Test
    public void testGetUserItems() {
        assertThrows(EntityNotFoundException.class, () ->
                itemService.getUserItems(1, null, null, itemMapper));

        User user1 = userService.addUser(this.user1);
        User user2 = userService.addUser(this.user2);

        Item item1ByUser1 = itemService.addItem(this.item1ByUser1, user1.getId());
        Item item2ByUser1 = itemService.addItem(this.item2ByUser1, user1.getId());
        Item item3ByUser1 = itemService.addItem(this.item3ByUser1, user1.getId());
        Item item1ByUser2 = itemService.addItem(this.item1ByUser2, user2.getId());
        Item item2ByUser2 = itemService.addItem(this.item2ByUser2, user2.getId());

        Booking bookingLastToItem1ByUser1 = bookingService.addBooking(this.bookingLastToItem1ByUser1, user2.getId());
        Booking bookingNextToItem1ByUser1 = bookingService.addBooking(this.bookingNextToItem1ByUser1, user2.getId());
        Booking bookingLastToItem2ByUser2 = bookingService.addBooking(this.bookingLastToItem2ByUser2, user1.getId());
        Booking bookingNextToItem1ByUser2 = bookingService.addBooking(this.bookingNextToItem1ByUser2, user1.getId());

        bookingService.approveBooking(bookingLastToItem1ByUser1.getId(), user1.getId(), true);
        bookingService.approveBooking(bookingNextToItem1ByUser1.getId(), user1.getId(), true);
        bookingService.approveBooking(bookingLastToItem2ByUser2.getId(), user2.getId(), true);
        bookingService.approveBooking(bookingNextToItem1ByUser2.getId(), user2.getId(), true);

        Comment comment1ToItem1ByUser1 = itemService.addComment(
                this.comment1ToItem1ByUser1, item1ByUser1.getId(), user2.getId());
        Comment comment2ToItem1ByUser1 = itemService.addComment(
                this.comment2ToItem1ByUser1, item1ByUser1.getId(), user2.getId());

        List<ItemExtraInfoDto> itemsByUser1 = itemService.getUserItems(user1.getId(), null, null, itemMapper);

        assertThat(itemsByUser1.size()).isEqualTo(3);
        assertThat(itemsByUser1.get(0).getId()).isEqualTo(item1ByUser1.getId());
        assertThat(itemsByUser1.get(1).getId()).isEqualTo(item2ByUser1.getId());
        assertThat(itemsByUser1.get(2).getId()).isEqualTo(item3ByUser1.getId());
        assertThat(itemsByUser1.get(0).getLastBooking().getId()).isEqualTo(bookingLastToItem1ByUser1.getId());
        assertThat(itemsByUser1.get(0).getNextBooking().getId()).isEqualTo(bookingNextToItem1ByUser1.getId());
        assertThat(itemsByUser1.get(1).getNextBooking()).isNull();
        assertThat(itemsByUser1.get(1).getLastBooking()).isNull();
        assertThat(itemsByUser1.get(2).getNextBooking()).isNull();
        assertThat(itemsByUser1.get(2).getLastBooking()).isNull();
        assertThat(itemsByUser1.get(0).getComments().size()).isEqualTo(2);
        assertThat(itemsByUser1.get(0).getComments().get(0).getId()).isEqualTo(comment1ToItem1ByUser1.getId());
        assertThat(itemsByUser1.get(0).getComments().get(1).getId()).isEqualTo(comment2ToItem1ByUser1.getId());

        List<ItemExtraInfoDto> itemsByUser2 = itemService.getUserItems(user2.getId(), null, null, itemMapper);

        assertThat(itemsByUser2.size()).isEqualTo(2);
        assertThat(itemsByUser2.get(0).getId()).isEqualTo(item1ByUser2.getId());
        assertThat(itemsByUser2.get(1).getId()).isEqualTo(item2ByUser2.getId());
        assertThat(itemsByUser2.get(0).getNextBooking().getId()).isEqualTo(bookingNextToItem1ByUser2.getId());
        assertThat(itemsByUser2.get(1).getLastBooking().getId()).isEqualTo(bookingLastToItem2ByUser2.getId());
        assertThat(itemsByUser2.get(0).getLastBooking()).isNull();
        assertThat(itemsByUser2.get(1).getNextBooking()).isNull();
    }
}
