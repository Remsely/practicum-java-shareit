package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    Item addItem(Item item, long userId);

    Item updateItem(Item item, long itemId, long userId);

    Item getItem(long id);

    List<Item> getUserItems(long userId);

    List<Item> searchItems(String query);
}
