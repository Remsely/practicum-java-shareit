package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserCreationDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping
    public UserDto createUser(@Valid @RequestBody UserCreationDto userDto) {
        log.info("POST /users. Request body : {}", userDto);
        User user = userMapper.toEntity(userDto);
        return userMapper.toDto(userService.addUser(user));
    }

    @PatchMapping("/{id}")
    public UserDto updateUser(@Valid @RequestBody UserDto userDto, @PathVariable long id) {
        log.info("PATCH /users/{}. Request body : {}", id, userDto);
        User user = userMapper.toEntity(userDto);
        return userMapper.toDto(userService.updateUser(user, id));
    }

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable long id) {
        log.info("GET /users/{}", id);
        return userMapper.toDto(userService.getUser(id));
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable long id) {
        log.info("DELETE /users/{}", id);
        userService.deleteUser(id);
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        log.info("GET /users");
        return userMapper.toDtoList(userService.getUsers());
    }
}
