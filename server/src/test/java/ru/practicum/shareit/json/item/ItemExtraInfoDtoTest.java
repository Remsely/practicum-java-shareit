package ru.practicum.shareit.json.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingInsideItemDto;
import ru.practicum.shareit.item.dto.ItemExtraInfoDto;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemExtraInfoDtoTest {
    @Autowired
    private JacksonTester<ItemExtraInfoDto> json;

    @Test
    public void testItemExtraInfoDto() throws IOException {
        ItemExtraInfoDto dto = ItemExtraInfoDto.builder()
                .id(1L)
                .name("item")
                .description("description")
                .available(true)
                .lastBooking(BookingInsideItemDto.builder().id(1L).build())
                .nextBooking(BookingInsideItemDto.builder().id(2L).build())
                .build();

        JsonContent<ItemExtraInfoDto> result = json.write(dto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).extractingJsonPathStringValue("$.name")
                .isEqualTo(dto.getName());
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo(dto.getDescription());
        assertThat(result).extractingJsonPathBooleanValue("$.available")
                .isEqualTo(dto.getAvailable());
        assertThat(result).hasJsonPath("$.lastBooking.id");
        assertThat(result).hasJsonPath("$.nextBooking.id");
    }
}
