package ru.practicum.shareit.json.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingInsideItemDto;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.practicum.shareit.util.TestUtility.getStringFromDate;

@JsonTest
public class BookingInsideItemDtoTest {
    @Autowired
    private JacksonTester<BookingInsideItemDto> json;

    @Test
    public void testBookingInsideItemDto() throws IOException {
        BookingInsideItemDto bookingDto = BookingInsideItemDto.builder()
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .status(BookingStatus.WAITING)
                .bookerId(1L)
                .id(1L)
                .build();

        JsonContent<BookingInsideItemDto> result = json.write(bookingDto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).extractingJsonPathStringValue("$.start")
                .isEqualTo(getStringFromDate(bookingDto.getStart()));
        assertThat(result).extractingJsonPathStringValue("$.end")
                .isEqualTo(getStringFromDate(bookingDto.getEnd()));
        assertThat(result).extractingJsonPathStringValue("$.status")
                .isEqualTo(bookingDto.getStatus().toString());
        assertThat(result).hasJsonPath("$.bookerId");
    }
}
