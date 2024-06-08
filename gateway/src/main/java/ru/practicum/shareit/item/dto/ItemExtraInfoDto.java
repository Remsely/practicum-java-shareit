package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ItemExtraInfoDto {
    private long id;
    private String name;
    private String description;
    private Boolean available;
    private BookingInsideItemDto lastBooking;
    private BookingInsideItemDto nextBooking;
    private List<CommentDto> comments;
}
