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
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestCreationDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
public class ItemRequestControllerTest {
    private static User user;
    private static ItemRequest request;
    private static ItemRequestDto dto;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemRequestService requestService;

    @MockBean
    private ItemRequestMapper requestMapper;

    @Autowired
    private MockMvc mvc;

    @BeforeAll
    public static void setUp() {
        user = User.builder()
                .id(1L)
                .name("user")
                .email("test@test.com")
                .build();

        request = ItemRequest.builder()
                .description("description")
                .id(1L)
                .created(LocalDateTime.now())
                .user(user)
                .build();

        dto = ItemRequestDto.builder()
                .id(1L)
                .description("description")
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    public void testPostRequest_BlankDescription() throws Exception {
        ItemRequestCreationDto dto = ItemRequestCreationDto.builder()
                .description("  ")
                .build();

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testPostRequest_WithoutUser() throws Exception {
        ItemRequestCreationDto dto = ItemRequestCreationDto.builder()
                .description("description")
                .build();

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testPostRequest_UserNotFound() throws Exception {
        ItemRequestCreationDto dto = ItemRequestCreationDto.builder()
                .description("description")
                .build();

        when(requestMapper.toEntity(dto))
                .thenReturn(request);
        when(requestService.addRequest(Mockito.any(ItemRequest.class), Mockito.anyLong()))
                .thenThrow(new EntityNotFoundException(ErrorResponse.builder().build()));

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testPostRequest_Success() throws Exception {
        ItemRequestCreationDto creationDto = ItemRequestCreationDto.builder()
                .description("description")
                .build();

        when(requestMapper.toEntity(creationDto))
                .thenReturn(request);
        when(requestService.addRequest(Mockito.any(ItemRequest.class), Mockito.anyLong()))
                .thenReturn(request);
        when(requestMapper.toDto(Mockito.any(ItemRequest.class)))
                .thenReturn(dto);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(creationDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(dto.getDescription())))
                .andExpect(jsonPath("$.created", is(getStringFromDate(dto.getCreated()))))
                .andExpect(jsonPath("$.items", is(dto.getItems())));
    }

    @Test
    public void testGetUserItemsRequests_WithoutUser() throws Exception {
        mvc.perform(get("/requests")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetUserItemsRequests_UserNotFound() throws Exception {
        when(requestService.getUserRequests(Mockito.anyLong()))
                .thenThrow(new EntityNotFoundException(ErrorResponse.builder().build()));

        mvc.perform(get("/requests")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetUserItemsRequests_Success() throws Exception {
        when(requestService.getUserRequests(Mockito.anyLong()))
                .thenReturn(List.of(request, request));
        when(requestMapper.toDtoList(Mockito.anyList()))
                .thenReturn(List.of(dto, dto));

        mvc.perform(get("/requests")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(dto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(dto.getDescription())))
                .andExpect(jsonPath("$[0].created", is(getStringFromDate(dto.getCreated()))))
                .andExpect(jsonPath("$[0].items", is(dto.getItems())));
    }

    @Test
    public void testAllItemsRequests_WithoutUser() throws Exception {
        mvc.perform(get("/requests/all")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAllItemsRequests_UserNotFound() throws Exception {
        when(requestService.getAllRequests(
                Mockito.nullable(Integer.class), Mockito.nullable(Integer.class), Mockito.anyLong()
        )).thenThrow(new EntityNotFoundException(ErrorResponse.builder().build()));

        mvc.perform(get("/requests/all")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testAllItemsRequests_Success() throws Exception {
        when(requestService.getAllRequests(
                Mockito.nullable(Integer.class), Mockito.nullable(Integer.class), Mockito.anyLong()
        )).thenReturn(List.of(request, request));
        when(requestMapper.toDtoList(Mockito.anyList()))
                .thenReturn(List.of(dto, dto));

        mvc.perform(get("/requests/all")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(dto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(dto.getDescription())))
                .andExpect(jsonPath("$[0].created", is(getStringFromDate(dto.getCreated()))))
                .andExpect(jsonPath("$[0].items", is(dto.getItems())));
    }

    @Test
    public void testGetRequest_WithoutUser() throws Exception {
        mvc.perform(get("/requests/" + request.getId())
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetRequest_UserOrRequestNotFound() throws Exception {
        when(requestService.getRequest(Mockito.anyLong(), Mockito.anyLong()))
                .thenThrow(new EntityNotFoundException(ErrorResponse.builder().build()));

        mvc.perform(get("/requests/" + request.getId())
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetRequest_Success() throws Exception {
        when(requestService.getRequest(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(request);
        when(requestMapper.toDto(Mockito.any(ItemRequest.class)))
                .thenReturn(dto);

        mvc.perform(get("/requests/" + request.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(dto.getDescription())))
                .andExpect(jsonPath("$.created", is(getStringFromDate(dto.getCreated()))))
                .andExpect(jsonPath("$.items", is(dto.getItems())));
    }

    private String getStringFromDate(LocalDateTime date) {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
