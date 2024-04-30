package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemForOwnerDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    Item addItem(Item item, long userId);

    Item updateItem(Item item, long itemId, long userId);

    ItemForOwnerDto getItem(long id, long userId, ItemMapper itemMapper);

    List<ItemForOwnerDto> getUserItems(long userId, ItemMapper itemMapper);

    List<Item> searchItems(String query);
}
