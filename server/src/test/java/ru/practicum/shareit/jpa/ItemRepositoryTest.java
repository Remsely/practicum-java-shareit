package ru.practicum.shareit.jpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
public class ItemRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User user1;
    private User user2;
    private Item item1ByUser1;
    private Item item2ByUser1;
    private Item item1ByUser2;

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

        item1ByUser1 = Item.builder()
                .owner(user1)
                .available(true)
                .description("cool item")
                .name("item")
                .build();

        item2ByUser1 = Item.builder()
                .owner(user1)
                .available(true)
                .description("item")
                .name("cool")
                .build();

        item1ByUser2 = Item.builder()
                .owner(user2)
                .available(true)
                .description("none")
                .name("none")
                .build();
    }

    @Test
    public void testFindByOwnerOrderById() {
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(item1ByUser1);
        entityManager.persist(item2ByUser1);
        entityManager.persist(item1ByUser2);

        Pageable pageable = Pageable.unpaged();

        List<Item> requests = itemRepository.findByOwnerOrderById(user1, pageable);

        assertThat(requests.size()).isEqualTo(2);
        assertThat(requests.get(0)).isEqualTo(item1ByUser1);
        assertThat(requests.get(1)).isEqualTo(item2ByUser1);

        requests = itemRepository.findByOwnerOrderById(user2, pageable);

        assertThat(requests.size()).isEqualTo(1);
        assertThat(requests.get(0)).isEqualTo(item1ByUser2);
    }

    @Test
    public void testSearchByNameOrDescription() {
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(item1ByUser1);
        entityManager.persist(item2ByUser1);
        entityManager.persist(item1ByUser2);

        Pageable pageable = Pageable.unpaged();

        List<Item> requests = itemRepository.searchByNameOrDescription("aaaaa", pageable);
        assertThat(requests.size()).isEqualTo(0);

        requests = itemRepository.searchByNameOrDescription("CoOL", pageable);
        assertThat(requests.size()).isEqualTo(2);
    }
}
