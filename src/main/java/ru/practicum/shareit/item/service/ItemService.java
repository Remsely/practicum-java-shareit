package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemExtraInfoDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    Item addItem(Item item, long userId);

    Item updateItem(Item item, long itemId, long userId);

    ItemExtraInfoDto getItem(long id, long userId, ItemMapper itemMapper);

    List<ItemExtraInfoDto> getUserItems(long userId, ItemMapper itemMapper);

    List<Item> searchItems(String query);

    Comment addComment(Comment comment, long itemId, long userId);
}
