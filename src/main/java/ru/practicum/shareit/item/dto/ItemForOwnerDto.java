package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingInsideItemDto;

@Data
@Builder
public class ItemForOwnerDto {
    private long id;
    private String name;
    private String description;
    private Boolean available;
    private BookingInsideItemDto lastBooking;
    private BookingInsideItemDto nextBooking;
}
