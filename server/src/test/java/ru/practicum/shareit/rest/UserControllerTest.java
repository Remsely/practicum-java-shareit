package ru.practicum.shareit.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.model.ErrorResponse;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserCreationDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    private static User user;
    private static UserDto dto;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    @Autowired
    private MockMvc mvc;

    @BeforeAll
    public static void setUp() {
        user = User.builder()
                .id(1L)
                .name("user")
                .email("test@test.com")
                .build();

        dto = UserDto.builder()
                .id(1L)
                .name("user")
                .email("test@test.com")
                .build();
    }

    @Test
    public void testPostUser_NameIsEmpty() throws Exception {
        UserCreationDto dto = UserCreationDto.builder()
                .name("  ")
                .email("test@test.com")
                .build();

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testPostUser_EmailIsNotValid() throws Exception {
        UserCreationDto dto = UserCreationDto.builder()
                .name("user")
                .email("test")
                .build();

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testPostUser_Success() throws Exception {
        UserCreationDto creationDto = UserCreationDto.builder()
                .name("user")
                .email("test@test.com")
                .build();

        when(userMapper.toEntity(Mockito.any(UserCreationDto.class)))
                .thenReturn(user);
        when(userService.addUser(Mockito.any(User.class)))
                .thenReturn(user);
        when(userMapper.toDto(Mockito.any(User.class)))
                .thenReturn(dto);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(creationDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(dto.getName())))
                .andExpect(jsonPath("$.email", is(dto.getEmail())));
    }

    @Test
    public void testUpdateUser_EmailIsNotValid() throws Exception {
        UserDto dto = UserDto.builder()
                .name("user")
                .email("test")
                .build();

        mvc.perform(patch("/users/" + user.getId())
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateUser_UserDoesNotExists() throws Exception {
        when(userMapper.toEntity(Mockito.any(UserDto.class)))
                .thenReturn(user);
        when(userService.updateUser(Mockito.any(User.class), Mockito.anyLong()))
                .thenThrow(new EntityNotFoundException(ErrorResponse.builder().build()));

        mvc.perform(patch("/users/" + user.getId())
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateUser_Success() throws Exception {
        when(userMapper.toEntity(Mockito.any(UserDto.class)))
                .thenReturn(user);
        when(userService.updateUser(Mockito.any(User.class), Mockito.anyLong()))
                .thenReturn(user);
        when(userMapper.toDto(Mockito.any(User.class)))
                .thenReturn(dto);

        mvc.perform(patch("/users/" + user.getId())
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(dto.getName())))
                .andExpect(jsonPath("$.email", is(dto.getEmail())));
    }

    @Test
    public void testGetUser_UserDoesNotExists() throws Exception {
        when(userMapper.toEntity(Mockito.any(UserDto.class)))
                .thenReturn(user);
        when(userService.getUser(Mockito.anyLong()))
                .thenThrow(new EntityNotFoundException(ErrorResponse.builder().build()));

        mvc.perform(get("/users/" + user.getId())
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetUser_Success() throws Exception {
        when(userMapper.toEntity(Mockito.any(UserDto.class)))
                .thenReturn(user);
        when(userService.getUser(Mockito.anyLong()))
                .thenReturn(user);
        when(userMapper.toDto(Mockito.any(User.class)))
                .thenReturn(dto);

        mvc.perform(get("/users/" + user.getId())
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(dto.getName())))
                .andExpect(jsonPath("$.email", is(dto.getEmail())));
    }

    @Test
    public void testDeleteUser_UserDoesNotExists() throws Exception {
        when(userMapper.toEntity(Mockito.any(UserDto.class)))
                .thenReturn(user);
        doThrow(new EntityNotFoundException(ErrorResponse.builder().build()))
                .when(userService).deleteUser(Mockito.anyLong());

        mvc.perform(delete("/users/" + user.getId())
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteUser_Success() throws Exception {
        when(userMapper.toEntity(Mockito.any(UserDto.class)))
                .thenReturn(user);
        doNothing()
                .when(userService).deleteUser(Mockito.anyLong());

        mvc.perform(delete("/users/" + user.getId())
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetAllUsers_Success() throws Exception {
        when(userService.getUsers())
                .thenReturn(List.of(user, user));
        when(userMapper.toDtoList(Mockito.anyList()))
                .thenReturn(List.of(dto, dto));

        mvc.perform(get("/users")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(dto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(dto.getName())))
                .andExpect(jsonPath("$[0].email", is(dto.getEmail())));
    }
}
