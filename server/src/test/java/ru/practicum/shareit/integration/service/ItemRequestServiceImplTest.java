package ru.practicum.shareit.integration.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceImplTest {
    private final ItemRequestService requestService;
    private final UserService userService;
    private User user1;
    private User user2;
    private ItemRequest request1byUser1;
    private ItemRequest request2byUser1;
    private ItemRequest request3byUser1;
    private ItemRequest request1byUser2;
    private ItemRequest request2byUser2;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .name("user")
                .email("email@email.com")
                .build();

        user2 = User.builder()
                .name("user2")
                .email("email2@email.com")
                .build();

        request1byUser1 = ItemRequest.builder()
                .description("description1 user1")
                .build();

        request2byUser1 = ItemRequest.builder()
                .description("description2 user1")
                .build();

        request3byUser1 = ItemRequest.builder()
                .description("description3 user1")
                .build();

        request1byUser2 = ItemRequest.builder()
                .description("description1 user2")
                .build();

        request2byUser2 = ItemRequest.builder()
                .description("description2 user2")
                .build();
    }

    @Test
    public void testGetAllRequests() {
        assertThrows(EntityNotFoundException.class, () ->
                requestService.getAllRequests(null, null, 1));

        User user1 = userService.addUser(this.user1);
        User user2 = userService.addUser(this.user2);

        ItemRequest request1ByUser1 = requestService.addRequest(this.request1byUser1, user1.getId());
        ItemRequest request2ByUser1 = requestService.addRequest(this.request2byUser1, user1.getId());
        ItemRequest request3ByUser1 = requestService.addRequest(this.request3byUser1, user1.getId());
        ItemRequest request1ByUser2 = requestService.addRequest(this.request1byUser2, user2.getId());
        ItemRequest request2ByUser2 = requestService.addRequest(this.request2byUser2, user2.getId());

        List<ItemRequest> requestsByUser2 = requestService.getAllRequests(null, null, user1.getId());

        assertThat(requestsByUser2.size()).isEqualTo(2);
        assertThat(requestsByUser2.get(0)).isEqualTo(request1ByUser2);
        assertThat(requestsByUser2.get(1)).isEqualTo(request2ByUser2);

        List<ItemRequest> requestsByUser1 = requestService.getAllRequests(null, null, user2.getId());

        assertThat(requestsByUser1.size()).isEqualTo(3);
        assertThat(requestsByUser1.get(0)).isEqualTo(request1ByUser1);
        assertThat(requestsByUser1.get(1)).isEqualTo(request2ByUser1);
        assertThat(requestsByUser1.get(2)).isEqualTo(request3ByUser1);

        requestsByUser2 = requestService.getAllRequests(0, 1, user1.getId());

        assertThat(requestsByUser2.size()).isEqualTo(1);
        assertThat(requestsByUser2.get(0)).isEqualTo(request1ByUser2);

        requestsByUser1 = requestService.getAllRequests(0, 2, user2.getId());

        assertThat(requestsByUser1.size()).isEqualTo(2);
        assertThat(requestsByUser1.get(0)).isEqualTo(request1ByUser1);
        assertThat(requestsByUser1.get(1)).isEqualTo(request2ByUser1);
    }
}
