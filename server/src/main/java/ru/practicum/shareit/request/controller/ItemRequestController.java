package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreationDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService requestService;
    private final ItemRequestMapper requestMapper;

    @PostMapping
    public ItemRequestDto postItemRequest(@RequestBody ItemRequestCreationDto dto,
                                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Post /requests (X-Sharer-User-id = {}). Request body : {}", userId, dto);
        ItemRequest request = requestMapper.toEntity(dto);
        return requestMapper.toDto(requestService.addRequest(request, userId));
    }

    @GetMapping
    public List<ItemRequestDto> getUserItemRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Get /requests (X-Sharer-User-id = {})", userId);
        return requestMapper.toDtoList(requestService.getUserRequests(userId));
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllItemRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                   @RequestParam(required = false) Integer from,
                                                   @RequestParam(required = false) Integer size) {
        log.info("Get /requests/all?from={}&size={} (X-Sharer-User-id = {})", from, size, userId);
        return requestMapper.toDtoList(requestService.getAllRequests(from, size, userId));
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                     @PathVariable Long requestId) {
        log.info("Get /requests/{}", requestId);
        return requestMapper.toDto(requestService.getRequest(requestId, userId));
    }
}
