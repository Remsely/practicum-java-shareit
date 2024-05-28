package ru.practicum.shareit.jpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
public class ItemRequestRepositoryTest {
    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User user1;
    private User user2;
    private ItemRequest request1FromUser1;
    private ItemRequest request2FromUser1;
    private ItemRequest request1FromUser2;

    @BeforeEach
    public void setUp() {
        user1 = User.builder()
                .name("user")
                .email("email@email.ru")
                .build();

        user2 = User.builder()
                .name("user2")
                .email("email2@email.ru")
                .build();

        request1FromUser1 = ItemRequest.builder()
                .user(user1)
                .description("description1")
                .created(LocalDateTime.now().minusDays(5))
                .build();

        request2FromUser1 = ItemRequest.builder()
                .user(user1)
                .description("description2")
                .created(LocalDateTime.now().minusDays(2))
                .build();

        request1FromUser2 = ItemRequest.builder()
                .user(user2)
                .description("description3")
                .created(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    public void testFindByUser() {
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(request1FromUser1);
        entityManager.persist(request2FromUser1);
        entityManager.persist(request1FromUser2);

        List<ItemRequest> requests = itemRequestRepository.findByUser(user1);
        assertThat(requests.size()).isEqualTo(2);

        requests = itemRequestRepository.findByUser(user2);
        assertThat(requests.size()).isEqualTo(1);
    }

    @Test
    public void testFindByUserNot() {
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(request1FromUser1);
        entityManager.persist(request2FromUser1);
        entityManager.persist(request1FromUser2);

        Pageable pageable = Pageable.unpaged();

        List<ItemRequest> requests = itemRequestRepository.findByUserNot(user1, pageable);
        assertThat(requests.size()).isEqualTo(1);

        requests = itemRequestRepository.findByUserNot(user2, pageable);
        assertThat(requests.size()).isEqualTo(2);
    }
}
