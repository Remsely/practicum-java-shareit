package ru.practicum.shareit.item.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentMapper {
    public Comment toEntity(CommentDto dto) {
        return Comment.builder()
                .text(dto.getText())
                .build();
    }

    public CommentDto toDto(Comment entity) {
        return CommentDto.builder()
                .id(entity.getId())
                .authorName(entity.getAuthor().getName())
                .created(entity.getCreated())
                .text(entity.getText())
                .build();
    }

    public List<CommentDto> toDtoList(List<Comment> comments) {
        return comments.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
