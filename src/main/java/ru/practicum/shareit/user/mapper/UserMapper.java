package ru.practicum.shareit.user.mapper;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

public interface UserMapper {
    UserDto toDto(User user);

    User fromDto(UserDto userDto);
}
