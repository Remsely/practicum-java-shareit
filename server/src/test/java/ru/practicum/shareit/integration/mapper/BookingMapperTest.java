package ru.practicum.shareit.integration.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingCreationDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class BookingMapperTest {
    @Autowired
    private BookingMapper bookingMapper;

    @Test
    public void testToEntity() {
        BookingCreationDto dto = BookingCreationDto.builder()
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(4))
                .itemId(1L)
                .build();

        Booking booking = bookingMapper.toEntity(dto);

        assertThat(booking.getId()).isNull();
        assertThat(booking.getStart()).isEqualTo(dto.getStart());
        assertThat(booking.getEnd()).isEqualTo(dto.getEnd());
        assertThat(booking.getItem().getId()).isEqualTo(dto.getItemId());
    }

    @Test
    public void testToDto() {
        User booker = User.builder().id(1L).build();
        Item item = Item.builder().id(1L).build();

        Booking booking = Booking.builder()
                .id(1L)
                .item(item)
                .booker(booker)
                .end(LocalDateTime.now().plusDays(5))
                .start(LocalDateTime.now().plusDays(4))
                .status(BookingStatus.WAITING)
                .build();

        BookingDto dto = bookingMapper.toDto(booking);

        assertThat(dto.getId()).isEqualTo(booking.getId());
        assertThat(dto.getStart()).isEqualTo(booking.getStart());
        assertThat(dto.getEnd()).isEqualTo(booking.getEnd());
        assertThat(dto.getItem().getId()).isEqualTo(booking.getItem().getId());
        assertThat(dto.getStatus()).isEqualTo(booking.getStatus());
        assertThat(dto.getBooker().getId()).isEqualTo(booking.getBooker().getId());
    }

    @Test
    public void testToDtoList() {
        User booker = User.builder().id(1L).build();
        Item item = Item.builder().id(1L).build();

        Booking booking = Booking.builder()
                .id(1L)
                .item(item)
                .booker(booker)
                .end(LocalDateTime.now().plusDays(5))
                .start(LocalDateTime.now().plusDays(4))
                .status(BookingStatus.WAITING)
                .build();

        List<Booking> bookings = List.of(booking, booking, booking);
        List<BookingDto> dtos = bookingMapper.toDtoList(bookings);

        assertThat(dtos.size()).isEqualTo(3);
        assertThat(dtos.get(0).getId()).isEqualTo(booking.getId());
        assertThat(dtos.get(0).getStart()).isEqualTo(booking.getStart());
        assertThat(dtos.get(0).getEnd()).isEqualTo(booking.getEnd());
        assertThat(dtos.get(0).getItem().getId()).isEqualTo(booking.getItem().getId());
        assertThat(dtos.get(0).getStatus()).isEqualTo(booking.getStatus());
        assertThat(dtos.get(0).getBooker().getId()).isEqualTo(booking.getBooker().getId());
    }
}
