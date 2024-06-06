package ru.practicum.shareit.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingInsideItemDto;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.IllegalPageableArgumentsException;
import ru.practicum.shareit.exception.ItemWasNotBeRentedException;
import ru.practicum.shareit.exception.UserWithoutAccessRightsException;
import ru.practicum.shareit.exception.model.ErrorResponse;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreationDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemExtraInfoDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {
    private static User user;
    private static Item item;
    private static ItemDto dto;
    private static ItemExtraInfoDto extraInfoDto;
    private static CommentDto commentDto;
    private static Comment comment;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemService itemService;

    @MockBean
    private ItemMapper itemMapper;

    @MockBean
    private CommentMapper commentMapper;

    @Autowired
    private MockMvc mvc;

    @BeforeAll
    public static void setUp() {
        user = User.builder()
                .id(1L)
                .name("user")
                .email("test@test.com")
                .build();

        item = Item.builder()
                .id(1L)
                .description("description")
                .owner(user)
                .available(true)
                .build();

        comment = Comment.builder()
                .id(1L)
                .text("text")
                .author(User.builder().id(2L).build())
                .created(LocalDateTime.now())
                .item(item)
                .build();

        commentDto = CommentDto.builder()
                .authorName("user2")
                .created(LocalDateTime.now())
                .text("text")
                .build();

        dto = ItemDto.builder()
                .id(1L)
                .description("description")
                .requestId(null)
                .available(true)
                .build();

        extraInfoDto = ItemExtraInfoDto.builder()
                .id(1L)
                .description("description")
                .nextBooking(null)
                .lastBooking(null)
                .comments(List.of(commentDto))
                .available(true)
                .build();
    }

    @Test
    public void testAddItem_BlankName() throws Exception {
        ItemCreationDto dto = ItemCreationDto.builder()
                .name("  ")
                .description("desc")
                .available(false)
                .build();
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddItem_BlankDescription() throws Exception {
        ItemCreationDto dto = ItemCreationDto.builder()
                .name("name")
                .description("  ")
                .available(false)
                .build();
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddItem_AvailableIsNull() throws Exception {
        ItemCreationDto dto = ItemCreationDto.builder()
                .name("name")
                .description("desc")
                .available(null)
                .build();
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddItem_WithoutUser() throws Exception {
        ItemCreationDto dto = ItemCreationDto.builder()
                .name("name")
                .description("desc")
                .available(true)
                .build();
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddItem_UserOrRequestNotFound() throws Exception {
        ItemCreationDto dto = ItemCreationDto.builder()
                .name("name")
                .description("desc")
                .available(true)
                .requestId(1L)
                .build();

        when(itemMapper.toEntity(Mockito.any(ItemCreationDto.class)))
                .thenReturn(item);
        when(itemService.addItem(Mockito.any(Item.class), Mockito.anyLong()))
                .thenThrow(new EntityNotFoundException(ErrorResponse.builder().build()));

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testAddItem_Success() throws Exception {
        ItemCreationDto creationDto = ItemCreationDto.builder()
                .name("name")
                .description("desc")
                .available(true)
                .build();

        when(itemMapper.toEntity(Mockito.any(ItemCreationDto.class)))
                .thenReturn(item);
        when(itemService.addItem(Mockito.any(Item.class), Mockito.anyLong()))
                .thenReturn(item);
        when(itemMapper.toDto(Mockito.any(Item.class)))
                .thenReturn(dto);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(creationDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(dto.getName())))
                .andExpect(jsonPath("$.description", is(dto.getDescription())))
                .andExpect(jsonPath("$.available", is(dto.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(dto.getRequestId())));
    }

    @Test
    public void testUpdateItem_WithoutUser() throws Exception {
        mvc.perform(patch("/items/" + item.getId())
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateItem_UserOrItemNotFound() throws Exception {
        when(itemMapper.toEntity(Mockito.any(ItemDto.class)))
                .thenReturn(item);
        when(itemService.updateItem(Mockito.any(Item.class), Mockito.anyLong(), Mockito.anyLong()))
                .thenThrow(new EntityNotFoundException(ErrorResponse.builder().build()));

        mvc.perform(patch("/items/" + item.getId())
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateItem_WithoutAccessRights() throws Exception {
        when(itemMapper.toEntity(Mockito.any(ItemDto.class)))
                .thenReturn(item);
        when(itemService.updateItem(Mockito.any(Item.class), Mockito.anyLong(), Mockito.anyLong()))
                .thenThrow(new UserWithoutAccessRightsException(ErrorResponse.builder().build()));

        mvc.perform(patch("/items/" + item.getId())
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateItem_Success() throws Exception {
        when(itemMapper.toEntity(Mockito.any(ItemDto.class)))
                .thenReturn(item);
        when(itemService.updateItem(Mockito.any(Item.class), Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(item);
        when(itemMapper.toDto(Mockito.any(Item.class)))
                .thenReturn(dto);

        mvc.perform(patch("/items/" + item.getId())
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(dto.getName())))
                .andExpect(jsonPath("$.description", is(dto.getDescription())))
                .andExpect(jsonPath("$.available", is(dto.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(dto.getRequestId())));
    }

    @Test
    public void testGetItem_WithoutUser() throws Exception {
        mvc.perform(get("/items/" + item.getId())
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetItem_UserOrItemNotFound() throws Exception {
        when(itemService.getItem(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(ItemMapper.class)))
                .thenThrow(new EntityNotFoundException(ErrorResponse.builder().build()));

        mvc.perform(get("/items/" + item.getId())
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetItem_Success() throws Exception {
        when(itemService.getItem(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(ItemMapper.class)))
                .thenReturn(extraInfoDto);

        mvc.perform(get("/items/" + item.getId())
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(extraInfoDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(extraInfoDto.getName())))
                .andExpect(jsonPath("$.description", is(extraInfoDto.getDescription())))
                .andExpect(jsonPath("$.available", is(extraInfoDto.getAvailable())))
                .andExpect(jsonPath("$.comments", hasSize(extraInfoDto.getComments().size())))
                .andExpect(jsonPath("$.lastBooking", equalTo(getAsMap(extraInfoDto.getLastBooking()))))
                .andExpect(jsonPath("$.nextBooking", equalTo(getAsMap(extraInfoDto.getNextBooking()))));
    }

    @Test
    public void testGetItems_WithoutUser() throws Exception {
        mvc.perform(get("/items")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetItems_UserNotFound() throws Exception {
        when(itemService.getUserItems(
                Mockito.anyLong(),
                Mockito.nullable(Integer.class),
                Mockito.nullable(Integer.class),
                Mockito.any(ItemMapper.class)
        )).thenThrow(new EntityNotFoundException(ErrorResponse.builder().build()));

        mvc.perform(get("/items")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetItems_UnavailablePageable() throws Exception {
        when(itemService.getUserItems(
                Mockito.anyLong(),
                Mockito.nullable(Integer.class),
                Mockito.nullable(Integer.class),
                Mockito.any(ItemMapper.class)
        )).thenThrow(new IllegalPageableArgumentsException(ErrorResponse.builder().build()));

        mvc.perform(get("/items")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetItems_Success() throws Exception {
        when(itemService.getUserItems(
                Mockito.anyLong(),
                Mockito.nullable(Integer.class),
                Mockito.nullable(Integer.class),
                Mockito.any(ItemMapper.class)
        )).thenReturn(List.of(extraInfoDto, extraInfoDto));

        mvc.perform(get("/items")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(extraInfoDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(extraInfoDto.getName())))
                .andExpect(jsonPath("$[0].description", is(extraInfoDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(extraInfoDto.getAvailable())))
                .andExpect(jsonPath("$[0].comments", hasSize(extraInfoDto.getComments().size())))
                .andExpect(jsonPath("$[0].lastBooking", equalTo(getAsMap(extraInfoDto.getLastBooking()))))
                .andExpect(jsonPath("$[0].nextBooking", equalTo(getAsMap(extraInfoDto.getNextBooking()))));
    }

    @Test
    public void testSearchItems_textIsBlank() throws Exception {
        mvc.perform(get("/items/search?text=%20")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void testSearchItems_textIsEmpty() throws Exception {
        mvc.perform(get("/items/search?text=")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void testSearchItems_Success() throws Exception {
        when(itemService.searchItems(
                Mockito.anyInt(),
                Mockito.anyString(),
                Mockito.nullable(Integer.class),
                Mockito.nullable(Integer.class)
        )).thenReturn(List.of(item, item));
        when(itemMapper.toDtoList(Mockito.anyList()))
                .thenReturn(List.of(dto, dto));

        mvc.perform(get("/items/search?text=%20")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(dto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(dto.getName())))
                .andExpect(jsonPath("$[0].description", is(dto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(dto.getAvailable())))
                .andExpect(jsonPath("$[0].requestId", is(dto.getRequestId())));
    }

    @Test
    public void testAddComment_BlankText() throws Exception {
        CommentDto dto = CommentDto.builder()
                .text("  ")
                .build();
        mvc.perform(post("/items/" + item.getId() + "/comment")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddComment_WithoutUser() throws Exception {
        CommentDto dto = CommentDto.builder()
                .text("text")
                .build();
        mvc.perform(post("/items/" + item.getId() + "/comment")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddComment_UserOrBookerNotFound() throws Exception {
        CommentDto dto = CommentDto.builder()
                .text("text")
                .build();

        when(commentMapper.toEntity(Mockito.any(CommentDto.class)))
                .thenReturn(comment);
        when(itemService.addComment(Mockito.any(Comment.class), Mockito.anyLong(), Mockito.anyLong()))
                .thenThrow(new EntityNotFoundException(ErrorResponse.builder().build()));

        mvc.perform(post("/items/" + item.getId() + "/comment")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testAddComment_ItemWasNotRented() throws Exception {
        CommentDto dto = CommentDto.builder()
                .text("text")
                .build();

        when(commentMapper.toEntity(Mockito.any(CommentDto.class)))
                .thenReturn(comment);
        when(itemService.addComment(Mockito.any(Comment.class), Mockito.anyLong(), Mockito.anyLong()))
                .thenThrow(new ItemWasNotBeRentedException(ErrorResponse.builder().build()));

        mvc.perform(post("/items/" + item.getId() + "/comment")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddComment_Success() throws Exception {
        CommentDto creationDto = CommentDto.builder()
                .text("text")
                .build();

        when(commentMapper.toEntity(Mockito.any(CommentDto.class)))
                .thenReturn(comment);
        when(itemService.addComment(Mockito.any(Comment.class), Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(comment);
        when(commentMapper.toDto(Mockito.any(Comment.class)))
                .thenReturn(commentDto);

        mvc.perform(post("/items/" + item.getId() + "/comment")
                        .content(mapper.writeValueAsString(creationDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())))
                .andExpect(jsonPath("$.created", is(getStringFromDate(commentDto.getCreated()))));
    }

    private Map<String, Object> getAsMap(BookingInsideItemDto booking) throws JsonProcessingException {
        return mapper.readValue(mapper.writeValueAsString(booking), new TypeReference<>() {
        });
    }

    private String getStringFromDate(LocalDateTime date) {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
