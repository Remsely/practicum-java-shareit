package ru.practicum.shareit.json.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.dto.ItemRequestCreationDto;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemRequestCreationDtoTest {
    @Autowired
    private JacksonTester<ItemRequestCreationDto> json;

    @Test
    public void testItemRequestCreationDto() throws IOException {
        ItemRequestCreationDto dto = ItemRequestCreationDto.builder()
                .description("description")
                .build();

        JsonContent<ItemRequestCreationDto> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo(dto.getDescription());
    }
}
