package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public Item addItem(Item item, long userId) {
        userRepository.checkUserExistence(userId);
        item.setOwner(userRepository.get(userId));

        Item addedItem = itemRepository.add(item);
        log.info("add Item: an item with an id {} and owner id {} has been added. Item : {}.",
                addedItem.getId(), userId, addedItem);
        return addedItem;
    }

    @Override
    public Item updateItem(Item item, long itemId, long userId) {
        userRepository.checkUserExistence(userId);
        item.setOwner(userRepository.get(userId));
        item.setId(itemId);

        Item updatedItem = itemRepository.update(item);
        log.info("update Item: an item with an id {} and owner id {} has been updated. Item : {}.",
                updatedItem.getId(), userId, updatedItem);
        return updatedItem;
    }

    @Override
    public Item getItem(long id) {
        Item item = itemRepository.get(id);
        log.info("get Item: a item with an id {} has been received. Item : {}.", item.getId(), item);
        return item;
    }

    @Override
    public List<Item> getUserItems(long userId) {
        userRepository.checkUserExistence(userId);
        List<Item> items = itemRepository.getByUserId(userId);
        log.info("get user's Items: the list of items of the user with id {} has been received. List : {}.",
                userId, items);
        return items;
    }

    @Override
    public List<Item> searchItems(String query) {
        List<Item> items = itemRepository.search(query);
        log.info("The list of items requested by query \"{}\" has been received. List {}.", query, items);
        return items;
    }
}
