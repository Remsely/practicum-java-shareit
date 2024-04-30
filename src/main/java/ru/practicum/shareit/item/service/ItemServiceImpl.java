package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingJpaRepository;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ItemWasNotBeRentedException;
import ru.practicum.shareit.exception.UserWithoutAccessRightsException;
import ru.practicum.shareit.exception.model.ErrorResponse;
import ru.practicum.shareit.item.dto.ItemGettingDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentJpaRepository;
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
    private final CommentJpaRepository commentRepository;

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
    public ItemGettingDto getItem(long id, long userId, ItemMapper itemMapper, CommentMapper commentMapper) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorResponse.builder()
                                .reason("User repository")
                                .error("User with id " + userId + " does not exist!")
                                .build()
                ));

        Item item = itemRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(
                ErrorResponse.builder()
                        .reason("Item repository")
                        .error("Item with id " + id + " does not exist!")
                        .build()
        ));

        List<Comment> comments = commentRepository.findByItem(item);

        if (item.getOwner().getId() != userId) {
            ItemGettingDto dto = itemMapper.toDto(item, null, null, commentMapper.toDtoList(comments));
            log.info("get Item: a item with an id {} has been received. Item : {}.", item.getId(), dto);
            return dto;
        }

        List<Booking> bookings = bookingRepository.findBookingsByItemAndStatus(item, BookingStatus.APPROVED);

        LocalDateTime now = LocalDateTime.now();

        Booking next = bookings.stream()
                .filter(b -> b.getStart().isAfter(now))
                .min(Comparator.comparing(Booking::getStart))
                .orElse(null);

        Booking last;
        if (next == null) {
            last = !bookings.isEmpty()
                    ? bookings.get(bookings.size() - 1)
                    : null;
        } else {
            last = bookings.stream()
                    .filter(b -> b.getEnd().isBefore(next.getStart()))
                    .max(Comparator.comparing(Booking::getEnd))
                    .orElse(null);
        }
        ItemGettingDto dto = itemMapper.toDto(item, next, last, commentMapper.toDtoList(comments));
        log.info("get Item: a item with an id {} has been received. Item : {}.", item.getId(), dto);
        return dto;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemGettingDto> getUserItems(long userId, ItemMapper itemMapper, CommentMapper commentMapper) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorResponse.builder()
                                .reason("User repository")
                                .error("User with id " + userId + " does not exist!")
                                .build()
                ));

        List<Item> items = itemRepository.findByOwner(owner);

        List<Comment> comments = commentRepository.findByItemIn(items);
        Map<Item, List<Comment>> commentsByItem = comments.stream().collect(Collectors.groupingBy(Comment::getItem));

        List<Booking> bookings = bookingRepository.findBookingsByItemInAndStatusOrderByItem(
                items, BookingStatus.APPROVED);
        Map<Item, List<Booking>> bookingsByItem = bookings.stream().collect(Collectors.groupingBy(Booking::getItem));

        List<ItemGettingDto> itemsWithLastAndNextBookings = items.stream()
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
                    return itemMapper.toDto(i, next, last, commentMapper.toDtoList(
                            commentsByItem.getOrDefault(i, List.of())
                    ));
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

    @Override
    public Comment addComment(Comment comment, long itemId, long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorResponse.builder()
                                .reason("Item repository")
                                .error("Item with id " + itemId + " does not exist!")
                                .build()
                ));
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorResponse.builder()
                                .reason("User repository")
                                .error("User with id " + userId + " does not exist!")
                                .build()
                ));
        if (!bookingRepository.existsByItemAndBookerAndStatusAndEndBefore(
                item, booker, BookingStatus.APPROVED, LocalDateTime.now())) {
            throw new ItemWasNotBeRentedException(ErrorResponse.builder()
                    .reason("Booking repository")
                    .error("The item with id " + itemId + " was not rented by the user with id " + userId + "!")
                    .build());
        }
        comment.setItem(item);
        comment.setAuthor(booker);
        comment.setCreated(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);

        log.info("add Comment: a comment with an id {} by user with an id {} to item with an id {} has been added. " +
                "Comment : {}.", savedComment.getId(), userId, itemId, savedComment);
        return savedComment;
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
