package ru.practicum.shareit.jpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class CommentRepositoryTest {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User owner;
    private User commentator;
    private Item item1;
    private Item item2;
    private Comment comment1ForItem1;
    private Comment comment2ForItem1;
    private Comment comment1ForItem2;

    @BeforeEach
    public void setUp() {
        owner = User.builder()
                .name("user")
                .email("email@email.ru")
                .build();

        commentator = User.builder()
                .name("user2")
                .email("email2@email.ru")
                .build();

        item1 = Item.builder()
                .available(true)
                .description("description")
                .owner(owner)
                .name("item")
                .build();

        item2 = Item.builder()
                .available(true)
                .description("description2")
                .owner(owner)
                .name("item2")
                .build();

        comment1ForItem1 = Comment.builder()
                .item(item1)
                .author(commentator)
                .text("text1")
                .created(LocalDateTime.now().minusDays(5))
                .build();

        comment2ForItem1 = Comment.builder()
                .item(item1)
                .author(commentator)
                .text("text2")
                .created(LocalDateTime.now().minusDays(3))
                .build();

        comment1ForItem2 = Comment.builder()
                .item(item2)
                .author(commentator)
                .text("text3")
                .created(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    public void testFindByItemIn() {
        entityManager.persist(owner);
        entityManager.persist(commentator);
        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.persist(comment1ForItem1);
        entityManager.persist(comment2ForItem1);
        entityManager.persist(comment1ForItem2);

        List<Comment> comments = commentRepository.findByItemIn(List.of(item1, item2));

        assertThat(comments).hasSize(3);
    }

    @Test
    public void testFindByItem() {
        entityManager.persist(owner);
        entityManager.persist(commentator);
        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.persist(comment1ForItem1);
        entityManager.persist(comment2ForItem1);
        entityManager.persist(comment1ForItem2);

        List<Comment> comments = commentRepository.findByItem(item1);

        assertThat(comments).hasSize(2);
    }
}
