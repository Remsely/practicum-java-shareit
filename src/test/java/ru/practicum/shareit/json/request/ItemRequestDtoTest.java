package ru.practicum.shareit.json.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.practicum.shareit.util.TestUtility.getStringFromDate;

@JsonTest
public class ItemRequestDtoTest {
    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    public void testItemRequestDto() throws IOException {
        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("description")
                .created(LocalDateTime.now())
                .items(List.of(ItemDto.builder().id(1L).build()))
                .build();

        JsonContent<ItemRequestDto> result = json.write(dto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo(dto.getDescription());
        assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo(getStringFromDate(dto.getCreated()));
        assertThat(result).extractingJsonPathArrayValue("$.items")
                .hasSize(1);
        assertThat(result).hasJsonPath("$.items[0].id");
    }
}
