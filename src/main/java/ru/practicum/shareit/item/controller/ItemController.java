package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemGettingDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @PostMapping
    public ItemDto addItem(@Valid @RequestBody ItemCreateDto itemDto,
                           @RequestHeader("X-Sharer-User-id") long userId) {
        log.info("Post /items (X-Sharer-User-id = {}). Request body : {}", userId, itemDto);
        Item item = itemMapper.toEntity(itemDto);
        return itemMapper.toDto(itemService.addItem(item, userId));
    }

    @PatchMapping("/{id}")
    public ItemDto updateItem(@Valid @RequestBody ItemDto itemDto,
                              @PathVariable long id,
                              @RequestHeader("X-Sharer-User-id") long userId) {
        log.info("Patch /items/{} (X-Sharer-User-id = {}). Request body : {}", id, userId, itemDto);
        Item item = itemMapper.toEntity(itemDto);
        return itemMapper.toDto(itemService.updateItem(item, id, userId));
    }

    @GetMapping("/{id}")
    public ItemGettingDto getItem(@PathVariable long id, @RequestHeader("X-Sharer-User-id") Long userId) {
        log.info("GET /items/{}", id);
        return itemService.getItem(id, userId, itemMapper, commentMapper);
    }

    @GetMapping
    public List<ItemGettingDto> getItems(@RequestHeader("X-Sharer-User-id") Long userId) {
        log.info("GET /items (X-Sharer-User-id = {})", userId);
        return itemService.getUserItems(userId, itemMapper, commentMapper);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        log.info("GET /items/search?text={}", text);
        if (text == null || text.isEmpty()) {
            return List.of();
        }
        return itemMapper.toDtoList(itemService.searchItems(text));
    }

    @PostMapping("/{id}/comment")
    public CommentDto addComment(@Valid @RequestBody CommentDto commentDto,
                              @PathVariable long id,
                              @RequestHeader("X-Sharer-User-id") Long userId) {
        log.info("POST /items/{}/comment (X-Sharer-User-id = {}). Request body : {}", id, userId, commentDto);
        Comment comment = commentMapper.toEntity(commentDto);
        return commentMapper.toDto(itemService.addComment(comment, id, userId));
    }
}
