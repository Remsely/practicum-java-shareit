package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemGettingDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    Item addItem(Item item, long userId);

    Item updateItem(Item item, long itemId, long userId);

    ItemGettingDto getItem(long id, long userId, ItemMapper itemMapper);

    List<ItemGettingDto> getUserItems(long userId, ItemMapper itemMapper);

    List<Item> searchItems(String query);

    Comment addComment(Comment comment, long itemId, long userId);
}
