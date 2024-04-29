package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
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
    public ItemDto getItem(@PathVariable long id) {
        log.info("GET /items/{}", id);
        return itemMapper.toDto(itemService.getItem(id));
    }

    @GetMapping
    public List<ItemDto> getItems(@RequestHeader("X-Sharer-User-id") Long userId) {
        log.info("GET /items (X-Sharer-User-id = {})", userId);
        return itemMapper.toDtoList(itemService.getUserItems(userId));
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        log.info("GET /items/search?text={}", text);
        if (text == null || text.isEmpty()) {
            return List.of();
        }
        return itemMapper.toDtoList(itemService.searchItems(text));
    }
}
