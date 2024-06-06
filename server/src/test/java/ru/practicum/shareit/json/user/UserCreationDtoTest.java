package ru.practicum.shareit.json.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.dto.UserCreationDto;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class UserCreationDtoTest {
    @Autowired
    private JacksonTester<UserCreationDto> json;

    @Test
    public void testUserCreationDto() throws IOException {
        UserCreationDto dto = UserCreationDto.builder()
                .id(1L)
                .name("name")
                .email("email@eamil.com")
                .build();

        JsonContent<UserCreationDto> result = json.write(dto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).extractingJsonPathStringValue("$.name")
                .isEqualTo(dto.getName());
        assertThat(result).extractingJsonPathStringValue("$.email")
                .isEqualTo(dto.getEmail());
    }
}
