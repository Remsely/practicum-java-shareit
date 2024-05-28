package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.UserWithoutAccessRightsException;
import ru.practicum.shareit.exception.model.ErrorResponse;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemStorage implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private long currentId = 1;


    @Override
    public Item add(Item item) {
        long id = currentId++;
        item.setId(id);
        items.put(id, item);
        return item;
    }

    @Override
    public Item update(Item item) {
        long id = item.getId();
        this.checkItemExistence(id);

        Item savedItem = items.get(id);

        checkOwner(savedItem, item.getOwner().getId());

        String name = item.getName();
        String description = item.getDescription();
        Boolean available = item.getAvailable();

        Item updatedItem = Item.builder()
                .id(id)
                .name(name == null ? savedItem.getName() : name)
                .description(description == null ? savedItem.getDescription() : description)
                .available(available == null ? savedItem.getAvailable() : available)
                .owner(item.getOwner())
                .build();

        items.put(id, updatedItem);
        return updatedItem;
    }

    @Override
    public Item get(long id) {
        this.checkItemExistence(id);
        return items.get(id);
    }

    @Override
    public List<Item> getByUserId(long user) {
        return items.values().stream()
                .filter(i -> i.getOwner().getId() == user)
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> search(String query) {
        return items.values().stream()
                .filter(i -> i.getAvailable() && (i.getName().toLowerCase().contains(query.toLowerCase()) ||
                        i.getDescription().toLowerCase().contains(query.toLowerCase()))
                ).collect(Collectors.toList());
    }

    @Override
    public void checkItemExistence(long id) {
        if (!items.containsKey(id)) {
            throw new EntityNotFoundException(ErrorResponse.builder()
                    .reason("Item repository")
                    .error("Item with id " + id + " already exist!")
                    .build()
            );
        }
    }

    @Override
    public void deleteUserItems(long userId) {
        items.entrySet().removeIf(entry -> entry.getValue().getOwner().getId() == userId);
    }

    private void checkOwner(Item item, long userId) {
        if (item.getOwner().getId() != userId) {
            throw new UserWithoutAccessRightsException(ErrorResponse.builder()
                    .reason("Forbidden for this id")
                    .error("The user with id " + userId + " does not have access to this item")
                    .build());
        }
    }
}
