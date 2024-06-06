package ru.practicum.shareit.unit.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UserMapperTest {
    private final UserMapper mapper = new UserMapper();

    @Test
    public void testToDtoFromUser() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("email")
                .build();

        UserDto dto = mapper.toDto(user);

        assertThat(dto.getId()).isEqualTo(user.getId());
        assertThat(dto.getName()).isEqualTo(user.getName());
        assertThat(dto.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    public void testToEntityFromUserDto() {
        UserDto dto = UserDto.builder()
                .id(1L)
                .name("name")
                .email("email")
                .build();

        User entity = mapper.toEntity(dto);

        assertThat(dto.getId()).isEqualTo(entity.getId());
        assertThat(dto.getName()).isEqualTo(entity.getName());
        assertThat(dto.getEmail()).isEqualTo(entity.getEmail());
    }

    @Test
    public void testToDtoList() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("email")
                .build();

        List<User> users = List.of(user, user, user);
        List<UserDto> dtos = mapper.toDtoList(users);

        assertThat(dtos.size()).isEqualTo(3);
        assertThat(dtos.get(0).getId()).isEqualTo(user.getId());
        assertThat(dtos.get(0).getName()).isEqualTo(user.getName());
        assertThat(dtos.get(0).getEmail()).isEqualTo(user.getEmail());
    }
}
