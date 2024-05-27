package ru.practicum.shareit.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

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


}
