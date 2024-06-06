package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestService {
    ItemRequest addRequest(ItemRequest request, long userId);

    List<ItemRequest> getUserRequests(long userId);

    List<ItemRequest> getAllRequests(Integer from, Integer size, long userId);

    ItemRequest getRequest(long requestId, long userId);
}
