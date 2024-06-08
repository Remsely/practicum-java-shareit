package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.service.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestCreationDto;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestClient client;

    @PostMapping
    public ResponseEntity<?> postItemRequest(@Valid @RequestBody ItemRequestCreationDto dto,
                                             @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Post /requests (X-Sharer-User-id = {}). Request body : {}", userId, dto);
        return client.postRequest(dto, userId);
    }

    @GetMapping
    public ResponseEntity<?> getUserItemRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Get /requests (X-Sharer-User-id = {})", userId);
        return client.getUserRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllItemRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @RequestParam(required = false) Integer from,
                                                @RequestParam(required = false) Integer size) {
        log.info("Get /requests/all?from={}&size={} (X-Sharer-User-id = {})", from, size, userId);
        return client.getAllItemRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<?> getRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                        @PathVariable Long requestId) {
        log.info("Get /requests/{}", requestId);
        return client.getRequest(requestId, userId);
    }
}
