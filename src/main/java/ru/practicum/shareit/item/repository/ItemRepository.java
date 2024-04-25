package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {
    Item add(Item item);

    Item update(Item item);

    Item get(long id);

    List<Item> getByUserId(long user);

    List<Item> search(String query);

    void checkItemExistence(long id);

    void deleteUserItems(long userId);
}
