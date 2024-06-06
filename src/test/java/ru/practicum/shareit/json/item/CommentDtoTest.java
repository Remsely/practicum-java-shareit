package ru.practicum.shareit.json.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.CommentDto;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class CommentDtoTest {
    @Autowired
    private JacksonTester<CommentDto> json;

    @Test
    public void testCommentDto() throws IOException {
        CommentDto dto = CommentDto.builder()
                .id(1L)
                .text("text")
                .authorName("author")
                .created(LocalDateTime.now())
                .build();

        JsonContent<CommentDto> result = json.write(dto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).extractingJsonPathStringValue("$.text")
                .isEqualTo(dto.getText());
        assertThat(result).hasJsonPath("$.created");
        assertThat(result).extractingJsonPathStringValue("$.authorName")
                .isEqualTo(dto.getAuthorName());
    }
}
