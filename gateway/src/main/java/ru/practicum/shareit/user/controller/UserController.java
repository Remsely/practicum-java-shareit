package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.service.UserClient;
import ru.practicum.shareit.user.dto.UserCreationDto;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserClient client;

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UserCreationDto userDto) {
        log.info("POST /users. Request body : {}", userDto);
        return client.postUser(userDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserDto userDto, @PathVariable long id) {
        log.info("PATCH /users/{}. Request body : {}", id, userDto);
        return client.patchUser(userDto, id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable long id) {
        log.info("GET /users/{}", id);
        return client.getUser(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable long id) {
        log.info("DELETE /users/{}", id);
        return client.deleteUser(id);
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        log.info("GET /users");
        return client.getUsers();
    }
}
