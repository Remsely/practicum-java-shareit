package ru.practicum.shareit.json.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemCreationDto;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemCreationDtoTest {
    @Autowired
    private JacksonTester<ItemCreationDto> json;

    @Test
    public void testItemCreationDto() throws IOException {
        ItemCreationDto dto = ItemCreationDto.builder()
                .id(1L)
                .name("item")
                .description("description")
                .available(true)
                .requestId(1L)
                .build();

        JsonContent<ItemCreationDto> result = json.write(dto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).hasJsonPath("$.requestId");
        assertThat(result).extractingJsonPathStringValue("$.name")
                .isEqualTo(dto.getName());
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo(dto.getDescription());
        assertThat(result).extractingJsonPathBooleanValue("$.available")
                .isEqualTo(dto.getAvailable());
    }
}
