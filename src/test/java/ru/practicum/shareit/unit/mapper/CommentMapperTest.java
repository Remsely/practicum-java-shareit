package ru.practicum.shareit.unit.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CommentMapperTest {
    private final CommentMapper mapper = new CommentMapper();

    @Test
    public void testToEntity() {
        CommentDto dto = CommentDto.builder()
                .id(1L)
                .text("text")
                .authorName("authorName")
                .build();

        Comment comment = mapper.toEntity(dto);

        assertThat(comment.getId()).isNull();
        assertThat(comment.getAuthor()).isNull();
        assertThat(comment.getText()).isEqualTo(dto.getText());
    }

    @Test
    public void testToDto() {
        User user = User.builder().id(1L).build();
        Item item = Item.builder().id(1L).build();

        Comment comment = Comment.builder()
                .id(1L)
                .created(LocalDateTime.now())
                .author(user)
                .text("text")
                .item(item)
                .build();

        CommentDto dto = mapper.toDto(comment);

        assertThat(dto.getId()).isEqualTo(comment.getId());
        assertThat(dto.getCreated()).isEqualTo(comment.getCreated());
        assertThat(dto.getText()).isEqualTo(comment.getText());
        assertThat(dto.getAuthorName()).isEqualTo(comment.getAuthor().getName());
    }

    @Test
    public void testToDtoList() {
        User user = User.builder().id(1L).build();
        Item item = Item.builder().id(1L).build();

        Comment comment = Comment.builder()
                .id(1L)
                .created(LocalDateTime.now())
                .author(user)
                .text("text")
                .item(item)
                .build();

        List<Comment> comments = List.of(comment, comment, comment);
        List<CommentDto> dtos = mapper.toDtoList(comments);

        assertThat(dtos.size()).isEqualTo(3);
        assertThat(dtos.get(0).getId()).isEqualTo(comment.getId());
        assertThat(dtos.get(0).getCreated()).isEqualTo(comment.getCreated());
        assertThat(dtos.get(0).getText()).isEqualTo(comment.getText());
        assertThat(dtos.get(0).getAuthorName()).isEqualTo(comment.getAuthor().getName());
    }
}
