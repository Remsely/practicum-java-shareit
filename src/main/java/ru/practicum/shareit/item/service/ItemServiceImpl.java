package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingJpaRepository;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.UserWithoutAccessRightsException;
import ru.practicum.shareit.exception.model.ErrorResponse;
import ru.practicum.shareit.item.dto.ItemForOwnerDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemJpaRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserJpaRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemJpaRepository itemRepository;
    private final UserJpaRepository userRepository;
    private final BookingJpaRepository bookingRepository;

    @Transactional
    @Override
    public Item addItem(Item item, long userId) {
        item.setOwner(userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorResponse.builder()
                                .reason("User repository")
                                .error("User with id " + userId + " does not exist!")
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
                        .error("Item with id " + itemId + " does not exist!")
                        .build()
        ));
        item.setOwner(userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException(
                ErrorResponse.builder()
                        .reason("User repository")
                        .error("User with id " + userId + " does not exist!")
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
                        .error("Item with id " + id + " does not exist!")
                        .build()
        ));
        log.info("get Item: a item with an id {} has been received. Item : {}.", item.getId(), item);
        return item;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemForOwnerDto> getUserItems(long userId, ItemMapper itemMapper) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorResponse.builder()
                                .reason("User repository")
                                .error("User with id " + userId + " does not exist!")
                                .build()
                ));

        List<Item> items = itemRepository.findByOwner(owner);
        List<Booking> bookings = bookingRepository.findBookingsByItemInOrderByItem(items);

        Map<Item, List<Booking>> bookingsByItem = bookings.stream().collect(Collectors.groupingBy(Booking::getItem));

        List<ItemForOwnerDto> itemsWithLastAndNextBookings = items.stream()
                .map(i -> {
                    List<Booking> bookingsForItem = bookingsByItem.getOrDefault(i, List.of());

                    LocalDateTime now = LocalDateTime.now();

                    Booking next = bookingsForItem.stream()
                            .filter(b -> b.getStart().isAfter(now))
                            .min(Comparator.comparing(Booking::getStart))
                            .orElse(null);

                    Booking last;
                    if (next == null) {
                        last = !bookingsForItem.isEmpty()
                                ? bookingsForItem.get(bookingsForItem.size() - 1)
                                : null;
                    } else {
                        last = bookingsForItem.stream()
                                .filter(b -> b.getEnd().isBefore(next.getStart()))
                                .max(Comparator.comparing(Booking::getEnd))
                                .orElse(null);
                    }
                    return itemMapper.toDto(i, next, last);
                })
                .collect(Collectors.toList());

        log.info("get user's Items: the list of items of the user with id {} has been received. List : {}.",
                userId, itemsWithLastAndNextBookings);
        return itemsWithLastAndNextBookings;
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
                    .error("User with id " + id + " does not exist!")
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
                    .error("The user with id " + userId + " does not have access to this item!")
                    .build());
        }
    }
}
