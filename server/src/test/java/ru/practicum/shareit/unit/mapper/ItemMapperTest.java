package ru.practicum.shareit.unit.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemExtraInfoDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ItemMapperTest {
    private final ItemMapper mapper = new ItemMapper(new CommentMapper());

    @Test
    public void testToItemDto() {
        User owner = User.builder().id(1L).build();

        Item item = Item.builder()
                .id(1L)
                .name("name")
                .description("description")
                .owner(owner)
                .available(true)
                .request(null)
                .build();

        ItemDto dto = mapper.toDto(item);

        assertThat(dto.getId()).isEqualTo(item.getId());
        assertThat(dto.getName()).isEqualTo(item.getName());
        assertThat(dto.getDescription()).isEqualTo(item.getDescription());
        assertThat(dto.getAvailable()).isEqualTo(item.getAvailable());
        assertThat(dto.getRequestId()).isNull();

        ItemRequest request = ItemRequest.builder().id(1L).build();
        item.setRequest(request);

        dto = mapper.toDto(item);

        assertThat(dto.getId()).isEqualTo(item.getId());
        assertThat(dto.getName()).isEqualTo(item.getName());
        assertThat(dto.getDescription()).isEqualTo(item.getDescription());
        assertThat(dto.getAvailable()).isEqualTo(item.getAvailable());
        assertThat(dto.getRequestId()).isEqualTo(item.getRequest().getId());
    }

    @Test
    public void testItemExtraInfoDto() {
        User owner = User.builder().id(1L).build();
        User author = User.builder().id(2L).build();

        Item item = Item.builder()
                .id(1L)
                .name("name")
                .description("description")
                .owner(owner)
                .available(true)
                .request(null)
                .build();

        Booking booking = Booking.builder()
                .id(1L)
                .booker(author)
                .build();
        Comment comment = Comment.builder()
                .id(1L)
                .author(author)
                .build();
        List<Comment> comments = List.of(comment, comment, comment);

        ItemExtraInfoDto dto = mapper.toDto(item, null, booking, comments);

        assertThat(dto.getId()).isEqualTo(item.getId());
        assertThat(dto.getName()).isEqualTo(item.getName());
        assertThat(dto.getDescription()).isEqualTo(item.getDescription());
        assertThat(dto.getAvailable()).isEqualTo(item.getAvailable());
        assertThat(dto.getNextBooking()).isNull();
        assertThat(dto.getLastBooking().getId()).isEqualTo(booking.getId());
        assertThat(dto.getComments().size()).isEqualTo(comments.size());
        assertThat(dto.getComments().get(0).getId()).isEqualTo(comment.getId());
    }

    @Test
    public void testToEntityFromItemDto() {
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("name")
                .description("description")
                .available(true)
                .build();

        Item item = mapper.toEntity(dto);

        assertThat(item.getId()).isEqualTo(dto.getId());
        assertThat(item.getName()).isEqualTo(dto.getName());
        assertThat(item.getDescription()).isEqualTo(dto.getDescription());
        assertThat(item.getAvailable()).isEqualTo(dto.getAvailable());
        assertThat(item.getRequest()).isNull();

        dto.setRequestId(1L);
        item = mapper.toEntity(dto);

        assertThat(item.getId()).isEqualTo(dto.getId());
        assertThat(item.getName()).isEqualTo(dto.getName());
        assertThat(item.getDescription()).isEqualTo(dto.getDescription());
        assertThat(item.getAvailable()).isEqualTo(dto.getAvailable());
        assertThat(item.getRequest().getId()).isEqualTo(dto.getRequestId());
    }

    @Test
    public void testToDtoList() {
        User owner = User.builder().id(1L).build();

        Item item = Item.builder()
                .id(1L)
                .name("name")
                .description("description")
                .owner(owner)
                .available(true)
                .request(null)
                .build();

        List<Item> items = List.of(item, item, item);
        List<ItemDto> dtos = mapper.toDtoList(items);

        assertThat(dtos.size()).isEqualTo(3);
        assertThat(dtos.get(0).getId()).isEqualTo(item.getId());
        assertThat(dtos.get(0).getName()).isEqualTo(item.getName());
        assertThat(dtos.get(0).getDescription()).isEqualTo(item.getDescription());
        assertThat(dtos.get(0).getAvailable()).isEqualTo(item.getAvailable());
        assertThat(dtos.get(0).getRequestId()).isNull();
    }
}
