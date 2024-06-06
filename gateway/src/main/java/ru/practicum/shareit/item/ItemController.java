package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreationDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<?> addItem(@RequestHeader("X-Sharer-User-id") Long userId,
                                     @Valid @RequestBody ItemCreationDto itemDto) {
        log.info("Post /items (X-Sharer-User-id = {}). Request body : {}", userId, itemDto);
        return itemClient.postItem(itemDto, userId);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateItem(@RequestHeader("X-Sharer-User-id") Long userId,
                                        @Valid @RequestBody ItemDto itemDto,
                                        @PathVariable long id) {
        log.info("Patch /items/{} (X-Sharer-User-id = {}). Request body : {}", id, userId, itemDto);
        return itemClient.patchItem(itemDto, id, userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getItem(@RequestHeader("X-Sharer-User-id") Long userId,
                                     @PathVariable long id) {
        log.info("GET /items/{}", id);
        return itemClient.getItem(id, userId);
    }

    @GetMapping
    public ResponseEntity<?> getItems(@RequestHeader("X-Sharer-User-id") Long userId,
                                      @RequestParam(required = false) Integer from,
                                      @RequestParam(required = false) Integer size) {
        log.info("GET /items?from={}&size={} (X-Sharer-User-id = {})", from, size, userId);
        return itemClient.getItems(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchItems(@RequestHeader("X-Sharer-User-id") Long userId,
                                         @RequestParam String text,
                                         @RequestParam(required = false) Integer from,
                                         @RequestParam(required = false) Integer size) {
        log.info("GET /items/search?text={}&from={}&size={}", text, from, size);
        if (text.isBlank()) {
            return ResponseEntity.ok(List.of());
        }
        return itemClient.searchItems(userId, text, from, size);
    }

    @PostMapping("/{id}/comment")
    public ResponseEntity<?> addComment(@RequestHeader("X-Sharer-User-id") Long userId,
                                        @Valid @RequestBody CommentDto commentDto,
                                        @PathVariable long id) {
        log.info("POST /items/{}/comment (X-Sharer-User-id = {}). Request body : {}", id, userId, commentDto);
        return itemClient.postComment(commentDto, id, userId);
    }
}
