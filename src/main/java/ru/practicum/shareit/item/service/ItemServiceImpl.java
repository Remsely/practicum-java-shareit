package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.UserWithoutAccessRightsException;
import ru.practicum.shareit.exception.model.ErrorResponse;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemJpaRepository;
import ru.practicum.shareit.user.repository.UserJpaRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemJpaRepository itemRepository;
    private final UserJpaRepository userRepository;

    @Transactional
    @Override
    public Item addItem(Item item, long userId) {
        item.setOwner(userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorResponse.builder()
                                .reason("User repository")
                                .message("User with id " + userId + " does not exist!")
                                .build()
                ))
        );
        Item addedItem = itemRepository.save(item);
        log.info("add Item: an item with an id {} and owner id {} has been added. Item : {}.",
                addedItem.getId(), userId, addedItem);
        return addedItem;
    }

    @Transactional
    @Override
    public Item updateItem(Item item, long itemId, long userId) {
        Item itemToUpdate = itemRepository.findById(itemId).orElseThrow(() -> new EntityNotFoundException(
                ErrorResponse.builder()
                        .reason("Item repository")
                        .message("Item with id " + itemId + " does not exist!")
                        .build()
        ));
        item.setOwner(userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException(
                ErrorResponse.builder()
                        .reason("User repository")
                        .message("User with id " + userId + " does not exist!")
                        .build()
        )));
        checkPermission(userId, itemToUpdate);
        updateNonNullProperties(itemToUpdate, item);

        Item updatedItem = itemRepository.save(itemToUpdate);
        log.info("update Item: an item with an id {} and owner id {} has been updated. Item : {}.",
                updatedItem.getId(), userId, updatedItem);
        return updatedItem;
    }

    @Transactional(readOnly = true)
    @Override
    public Item getItem(long id) {
        Item item = itemRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(
                ErrorResponse.builder()
                        .reason("Item repository")
                        .message("Item with id " + id + " does not exist!")
                        .build()
        ));
        log.info("get Item: a item with an id {} has been received. Item : {}.", item.getId(), item);
        return item;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Item> getUserItems(long userId) {
        List<Item> items = itemRepository.findByOwner(userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorResponse.builder()
                                .reason("User repository")
                                .message("User with id " + userId + " does not exist!")
                                .build()
                )));
        log.info("get user's Items: the list of items of the user with id {} has been received. List : {}.",
                userId, items);
        return items;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Item> searchItems(String query) {
        List<Item> items = itemRepository.search(query);
        log.info("The list of items requested by query \"{}\" has been received. List {}.", query, items);
        return items;
    }

    private void checkUserExistence(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException(ErrorResponse.builder()
                    .reason("User repository")
                    .message("User with id " + id + " does not exist!")
                    .build()
            );
        }
    }

    private void updateNonNullProperties(Item existingItem, Item newItem) {
        if (newItem.getName() != null) {
            existingItem.setName(newItem.getName());
        }
        if (newItem.getDescription() != null) {
            existingItem.setDescription(newItem.getDescription());
        }
        if (newItem.getAvailable() != null) {
            existingItem.setAvailable(newItem.getAvailable());
        }
    }

    private void checkPermission(long userId, Item item) {
        if (!item.getOwner().getId().equals(userId)) {
            throw new UserWithoutAccessRightsException(ErrorResponse.builder()
                    .reason("Forbidden for this id")
                    .message("The user with id " + userId + " does not have access to this item!")
                    .build());
        }
    }
}
