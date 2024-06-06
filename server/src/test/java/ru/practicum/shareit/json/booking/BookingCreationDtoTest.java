package ru.practicum.shareit.json.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingCreationDto;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.practicum.shareit.util.TestUtility.getStringFromDate;

@JsonTest
public class BookingCreationDtoTest {
    @Autowired
    private JacksonTester<BookingCreationDto> json;

    @Test
    public void testBookingCreationDto() throws IOException {
        BookingCreationDto bookingCreationDto = BookingCreationDto.builder()
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .itemId(1L)
                .build();

        JsonContent<BookingCreationDto> result = json.write(bookingCreationDto);

        assertThat(result).extractingJsonPathStringValue("$.start")
                .isEqualTo(getStringFromDate(bookingCreationDto.getStart()));
        assertThat(result).extractingJsonPathStringValue("$.end")
                .isEqualTo(getStringFromDate(bookingCreationDto.getEnd()));
    }
}
