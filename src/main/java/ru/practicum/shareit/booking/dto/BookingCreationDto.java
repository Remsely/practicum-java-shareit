package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@Builder
public class BookingCreationDto {
    private Long itemId;

    @NotNull
    @FutureOrPresent
    private Date start;

    @NotNull
    @Future
    private Date end;
}
