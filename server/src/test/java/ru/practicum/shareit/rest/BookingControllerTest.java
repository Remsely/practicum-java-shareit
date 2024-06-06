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
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.controller.BookingState;
import ru.practicum.shareit.booking.dto.BookingCreationDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.AlreadyApprovedException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.UnavailableItemException;
import ru.practicum.shareit.exception.UserWithoutAccessRightsException;
import ru.practicum.shareit.exception.model.ErrorResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.util.TestUtility.getStringFromDate;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {
    private static Booking booking;
    private static BookingDto dto;
    private static User user;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private BookingMapper bookingMapper;

    @Autowired
    private MockMvc mvc;

    @BeforeAll
    static void setUp() {
        user = User.builder()
                .id(1L)
                .name("user")
                .email("test@test.com")
                .build();

        Item item = Item.builder()
                .id(1L)
                .description("description")
                .owner(User.builder().id(2L).build())
                .available(true)
                .build();

        booking = Booking.builder()
                .id(1L)
                .booker(user)
                .item(item)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(4))
                .status(BookingStatus.WAITING)
                .build();

        dto = BookingDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(4))
                .status(BookingStatus.APPROVED)
                .booker(UserDto.builder().id(1L).build())
                .item(ItemDto.builder().id(1L).build())
                .build();
    }

    @Test
    public void testAddBooking_WithoutUser() throws Exception {
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddBooking_UserDoesNotExists() throws Exception {
        when(bookingMapper.toEntity(Mockito.any(BookingCreationDto.class)))
                .thenReturn(booking);
        when(bookingService.addBooking(Mockito.any(Booking.class), Mockito.anyLong()))
                .thenThrow(new EntityNotFoundException(ErrorResponse.builder().build()));

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testAddBooking_UnavailableItem() throws Exception {
        when(bookingMapper.toEntity(Mockito.any(BookingCreationDto.class)))
                .thenReturn(booking);
        when(bookingService.addBooking(Mockito.any(Booking.class), Mockito.anyLong()))
                .thenThrow(new UnavailableItemException(ErrorResponse.builder().build()));

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddBooking_Success() throws Exception {
        when(bookingMapper.toEntity(Mockito.any(BookingCreationDto.class)))
                .thenReturn(booking);
        when(bookingService.addBooking(Mockito.any(Booking.class), Mockito.anyLong()))
                .thenReturn(booking);
        when(bookingMapper.toDto(Mockito.any(Booking.class)))
                .thenReturn(dto);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(getStringFromDate(dto.getStart()))))
                .andExpect(jsonPath("$.end", is(getStringFromDate(dto.getEnd()))))
                .andExpect(jsonPath("$.status", equalTo(dto.getStatus().toString())))
                .andExpect(jsonPath("$.booker", equalTo(getAsMap(dto.getBooker()))))
                .andExpect(jsonPath("$.item", equalTo(getAsMap(dto.getItem()))));
    }

    @Test
    public void testApproveBooking_WithoutUser() throws Exception {
        mvc.perform(patch("/bookings/" + booking.getId() + "?approved=true")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testApproveBooking_BookingDoesNotExists() throws Exception {
        when(bookingService.approveBooking(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyBoolean()))
                .thenThrow(new EntityNotFoundException(ErrorResponse.builder().build()));

        mvc.perform(patch("/bookings/" + booking.getId() + "?approved=true")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testApproveBooking_UserIsNotOwner() throws Exception {
        when(bookingService.approveBooking(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyBoolean()))
                .thenThrow(new UserWithoutAccessRightsException(ErrorResponse.builder().build()));

        mvc.perform(patch("/bookings/" + booking.getId() + "?approved=true")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testApproveBooking_BookingAlreadyApproved() throws Exception {
        when(bookingService.approveBooking(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyBoolean()))
                .thenThrow(new AlreadyApprovedException(ErrorResponse.builder().build()));

        mvc.perform(patch("/bookings/" + booking.getId() + "?approved=true")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testApproveBooking_Success() throws Exception {
        when(bookingService.approveBooking(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyBoolean()))
                .thenReturn(booking);
        when(bookingMapper.toDto(Mockito.any(Booking.class)))
                .thenReturn(dto);

        mvc.perform(patch("/bookings/" + booking.getId() + "?approved=true")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(getStringFromDate(dto.getStart()))))
                .andExpect(jsonPath("$.end", is(getStringFromDate(dto.getEnd()))))
                .andExpect(jsonPath("$.status", equalTo(dto.getStatus().toString())))
                .andExpect(jsonPath("$.booker", equalTo(getAsMap(dto.getBooker()))))
                .andExpect(jsonPath("$.item", equalTo(getAsMap(dto.getItem()))))
                .andExpect(jsonPath("$.status", equalTo(BookingStatus.APPROVED.toString())));
    }

    @Test
    public void testGetBooking_WithoutUser() throws Exception {
        mvc.perform(get("/bookings/" + booking.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetBooking_BookingDoesNotExists() throws Exception {
        when(bookingService.getBookingById(Mockito.anyLong(), Mockito.anyLong()))
                .thenThrow(new EntityNotFoundException(ErrorResponse.builder().build()));

        mvc.perform(get("/bookings/" + booking.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetBooking_UserWithoutAccessRights() throws Exception {
        when(bookingService.getBookingById(Mockito.anyLong(), Mockito.anyLong()))
                .thenThrow(new UserWithoutAccessRightsException(ErrorResponse.builder().build()));

        mvc.perform(get("/bookings/" + booking.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetBooking_Success() throws Exception {
        when(bookingService.getBookingById(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(booking);
        when(bookingMapper.toDto(Mockito.any(Booking.class)))
                .thenReturn(dto);

        mvc.perform(get("/bookings/" + booking.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(getStringFromDate(dto.getStart()))))
                .andExpect(jsonPath("$.end", is(getStringFromDate(dto.getEnd()))))
                .andExpect(jsonPath("$.status", equalTo(dto.getStatus().toString())))
                .andExpect(jsonPath("$.booker", equalTo(getAsMap(dto.getBooker()))))
                .andExpect(jsonPath("$.item", equalTo(getAsMap(dto.getItem()))))
                .andExpect(jsonPath("$.status", equalTo(BookingStatus.APPROVED.toString())));
    }

    @Test
    public void testGetUserBookings_WithoutUser() throws Exception {
        mvc.perform(get("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetUserBookings_UserDoesNotExists() throws Exception {
        when(bookingService.getUserBookings(
                Mockito.anyLong(),
                Mockito.any(BookingState.class),
                Mockito.nullable(Integer.class),
                Mockito.nullable(Integer.class)
        )).thenThrow(new EntityNotFoundException(ErrorResponse.builder().build()));

        mvc.perform(get("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetUserBookings_Success() throws Exception {
        when(bookingService.getUserBookings(
                Mockito.anyLong(),
                Mockito.any(BookingState.class),
                Mockito.nullable(Integer.class),
                Mockito.nullable(Integer.class)
        )).thenReturn(List.of(booking, booking));
        when(bookingMapper.toDtoList(Mockito.anyList()))
                .thenReturn(List.of(dto, dto));

        mvc.perform(get("/bookings/")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(dto.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(getStringFromDate(dto.getStart()))))
                .andExpect(jsonPath("$[0].end", is(getStringFromDate(dto.getEnd()))))
                .andExpect(jsonPath("$[0].status", equalTo(dto.getStatus().toString())))
                .andExpect(jsonPath("$[0].booker", equalTo(getAsMap(dto.getBooker()))))
                .andExpect(jsonPath("$[0].item", equalTo(getAsMap(dto.getItem()))))
                .andExpect(jsonPath("$[0].status", equalTo(BookingStatus.APPROVED.toString())));
    }

    @Test
    public void testGetUserItemsBookings_WithoutUser() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetUserItemsBookings_UserDoesNotExists() throws Exception {
        when(bookingService.getUserItemsBookings(
                Mockito.anyLong(),
                Mockito.any(BookingState.class),
                Mockito.nullable(Integer.class),
                Mockito.nullable(Integer.class)
        )).thenThrow(new EntityNotFoundException(ErrorResponse.builder().build()));

        mvc.perform(get("/bookings/owner")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetUserItemsBookings_Success() throws Exception {
        when(bookingService.getUserItemsBookings(
                Mockito.anyLong(),
                Mockito.any(BookingState.class),
                Mockito.nullable(Integer.class),
                Mockito.nullable(Integer.class)
        )).thenReturn(List.of(booking, booking));
        when(bookingMapper.toDtoList(Mockito.anyList()))
                .thenReturn(List.of(dto, dto));

        mvc.perform(get("/bookings/owner")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-id", user.getId().toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(dto.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(getStringFromDate(dto.getStart()))))
                .andExpect(jsonPath("$[0].end", is(getStringFromDate(dto.getEnd()))))
                .andExpect(jsonPath("$[0].status", equalTo(dto.getStatus().toString())))
                .andExpect(jsonPath("$[0].booker", equalTo(getAsMap(dto.getBooker()))))
                .andExpect(jsonPath("$[0].item", equalTo(getAsMap(dto.getItem()))))
                .andExpect(jsonPath("$[0].status", equalTo(BookingStatus.APPROVED.toString())));
    }

    private Map<String, Object> getAsMap(UserDto booker) throws JsonProcessingException {
        return mapper.readValue(mapper.writeValueAsString(booker), new TypeReference<>() {
        });
    }

    private Map<String, Object> getAsMap(ItemDto item) throws JsonProcessingException {
        return mapper.readValue(mapper.writeValueAsString(item), new TypeReference<>() {
        });
    }
}
