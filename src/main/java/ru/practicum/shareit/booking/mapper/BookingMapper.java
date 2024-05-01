package ru.practicum.shareit.booking.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingCreationDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BookingMapper {
    private final UserMapper userMapper;
    private final ItemMapper itemMapper;

    public Booking toEntity(final BookingCreationDto dto) {
        return Booking.builder()
                .start(dto.getStart())
                .end(dto.getEnd())
                .item(Item.builder().id(dto.getItemId()).build())
                .build();
    }

    public BookingDto toDto(final Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .booker(userMapper.toDto(booking.getBooker()))
                .item(itemMapper.toDto(booking.getItem()))
                .status(booking.getStatus())
                .build();
    }

    public List<BookingDto> toDtoList(final List<Booking> bookings) {
        return bookings.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
