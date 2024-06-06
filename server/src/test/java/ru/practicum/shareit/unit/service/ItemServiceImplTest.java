package ru.practicum.shareit.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.common.utils.PageableUtility;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ItemWasNotBeRentedException;
import ru.practicum.shareit.exception.UserWithoutAccessRightsException;
import ru.practicum.shareit.item.dto.ItemExtraInfoDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemServiceImplTest {
    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private PageableUtility pageableUtility;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    private Booking simpleBooking;

    private User simpleUser;

    private User otherOwner;

    private Item simpleItem;

    private ItemRequest simpleItemRequest;

    private Comment simpleComment;

    private ItemExtraInfoDto extraInfoDto;

    @BeforeEach
    public void setUp() {
        simpleUser = User.builder()
                .id(1L)
                .name("user")
                .email("user@user.com")
                .build();

        simpleItem = Item.builder()
                .id(1L)
                .name("name")
                .description("description")
                .build();

        simpleBooking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(2))
                .item(simpleItem)
                .build();

        otherOwner = User.builder()
                .id(2L)
                .build();

        simpleItemRequest = ItemRequest.builder()
                .id(1L)
                .description("description")
                .build();

        simpleComment = Comment.builder()
                .id(1L)
                .item(simpleItem)
                .text("text")
                .build();

        extraInfoDto = ItemExtraInfoDto.builder()
                .id(1L)
                .description("description")
                .build();
    }

    @Test
    public void testAddItem_OwnerDoesNotExist() {
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.addItem(simpleItem, 1));
        verify(userRepository).findById(1L);
    }

    @Test
    public void testAddItem_Success() {
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleUser));
        when(itemRepository.save(Mockito.any(Item.class)))
                .thenReturn(simpleItem);

        Item item = itemService.addItem(simpleItem, 1);

        assertEquals(item.getOwner(), simpleUser);
        verify(userRepository).findById(1L);
        verify(itemRepository).save(simpleItem);
    }

    @Test
    public void testAddItem_WithRequestDoesNotExists() {
        simpleItem.setRequest(simpleItemRequest);

        when(requestRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.addItem(simpleItem, 1));
        verify(requestRepository).findById(1L);
    }

    @Test
    public void testAddItem_WithRequestExist() {
        simpleItem.setRequest(simpleItemRequest);

        when(requestRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleItemRequest));
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleUser));
        when(itemRepository.save(Mockito.any(Item.class)))
                .thenReturn(simpleItem);

        Item item = itemService.addItem(simpleItem, 1);

        assertEquals(item.getOwner(), simpleUser);
        assertEquals(item.getRequest(), simpleItemRequest);

        verify(userRepository).findById(1L);
        verify(itemRepository).save(simpleItem);
        verify(requestRepository).findById(1L);
    }

    @Test
    public void testUpdateItem_ItemDoesNotExist() {
        when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.updateItem(simpleItem, 1, 1));
        verify(itemRepository).findById(1L);
    }

    @Test
    public void testUpdateItem_OwnerDoesNotExist() {
        simpleItem.setOwner(otherOwner);

        when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleItem));
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.updateItem(simpleItem, 1, 1));
        verify(itemRepository).findById(1L);
        verify(userRepository).findById(1L);
    }

    @Test
    public void testUpdateItem_UserWithoutPermission() {
        simpleItem.setOwner(simpleUser);

        when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleItem));
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(otherOwner));

        assertThrows(UserWithoutAccessRightsException.class, () ->
                itemService.updateItem(Item.builder().build(), 1, 2));
        verify(itemRepository).findById(1L);
        verify(userRepository).findById(2L);
    }

    @Test
    public void testUpdateItem_Success() {
        simpleItem.setOwner(simpleUser);
        simpleItem.setAvailable(false);

        when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleItem));
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleUser));
        when(itemRepository.save(Mockito.any(Item.class)))
                .thenReturn(simpleItem);

        assertEquals(simpleItem, itemService.updateItem(simpleItem, 1, 1));

        verify(itemRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(itemRepository).save(simpleItem);
    }

    @Test
    public void testUpdateItem_WithoutChangesSuccess() {
        simpleItem.setOwner(simpleUser);
        simpleItem.setAvailable(false);

        when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleItem));
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleUser));
        when(itemRepository.save(Mockito.any(Item.class)))
                .thenReturn(simpleItem);

        assertEquals(simpleItem, itemService.updateItem(Item.builder().build(), 1, 1));

        verify(itemRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(itemRepository).save(simpleItem);
    }

    @Test
    public void testGetItem_UserDoesNotExist() {
        when(userRepository.existsById(Mockito.anyLong()))
                .thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> itemService.getItem(1, 1, itemMapper));
        verify(userRepository).existsById(1L);
    }

    @Test
    public void testGetItem_ItemDoesNotExist() {
        when(userRepository.existsById(Mockito.anyLong()))
                .thenReturn(true);
        when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.getItem(1, 1, itemMapper));
        verify(userRepository).existsById(1L);
        verify(itemRepository).findById(1L);
    }

    @Test
    public void testGetItem_ByNotOwner() {
        simpleItem.setOwner(otherOwner);

        when(userRepository.existsById(Mockito.anyLong()))
                .thenReturn(true);
        when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleItem));
        when(commentRepository.findByItem(Mockito.any(Item.class)))
                .thenReturn(List.of(simpleComment));
        when(itemMapper.toDto(
                Mockito.any(Item.class),
                Mockito.nullable(Booking.class),
                Mockito.nullable(Booking.class),
                Mockito.anyList())
        ).thenReturn(extraInfoDto);

        ItemExtraInfoDto dto = itemService.getItem(1, 1, itemMapper);

        assertEquals(extraInfoDto, dto);
        verify(userRepository).existsById(1L);
        verify(itemRepository).findById(1L);
        verify(commentRepository).findByItem(simpleItem);
        verify(itemMapper).toDto(simpleItem, null, null, List.of(simpleComment));
    }

    @Test
    public void testGetItem_ByOwner() {
        simpleItem.setOwner(simpleUser);
        simpleBooking.setStatus(BookingStatus.APPROVED);
        simpleBooking.setEnd(LocalDateTime.now().minusDays(2));

        when(userRepository.existsById(Mockito.anyLong()))
                .thenReturn(true);
        when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleItem));
        when(commentRepository.findByItem(Mockito.any(Item.class)))
                .thenReturn(List.of(simpleComment));
        when(itemMapper.toDto(
                Mockito.any(Item.class),
                Mockito.nullable(Booking.class),
                Mockito.nullable(Booking.class),
                Mockito.anyList())
        ).thenReturn(extraInfoDto);
        when(bookingRepository.findByItemAndStatus(Mockito.any(Item.class), Mockito.any(BookingStatus.class)))
                .thenReturn(List.of(simpleBooking));

        ItemExtraInfoDto dto = itemService.getItem(1, 1, itemMapper);

        assertEquals(extraInfoDto, dto);
        verify(userRepository).existsById(1L);
        verify(itemRepository).findById(1L);
        verify(commentRepository).findByItem(simpleItem);
        verify(bookingRepository).findByItemAndStatus(simpleItem, BookingStatus.APPROVED);
        verify(itemMapper).toDto(simpleItem, null, simpleBooking, List.of(simpleComment));
    }

    @Test
    public void testGetUserItems_UserDoesNotExists() {
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                itemService.getUserItems(1, 0, 2, itemMapper));
        verify(userRepository).findById(1L);
    }

    @Test
    public void testGetUserItems_Success() {
        simpleBooking.setStatus(BookingStatus.APPROVED);
        simpleBooking.setEnd(LocalDateTime.now().minusDays(2));

        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleUser));
        when(pageableUtility.getPageableFromArguments(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Pageable.unpaged());
        when(itemRepository.findByOwnerOrderById(Mockito.any(User.class), Mockito.any(Pageable.class)))
                .thenReturn(List.of(simpleItem));
        when(commentRepository.findByItemIn(Mockito.anyList()))
                .thenReturn(List.of(simpleComment));
        when(bookingRepository.findByItemInAndStatusOrderByItem(
                Mockito.anyList(),
                Mockito.any(BookingStatus.class))
        ).thenReturn(List.of(simpleBooking));
        when(itemMapper.toDto(
                Mockito.any(Item.class),
                Mockito.nullable(Booking.class),
                Mockito.nullable(Booking.class),
                Mockito.anyList())
        ).thenReturn(extraInfoDto);

        List<ItemExtraInfoDto> itemExtraInfoDtos = itemService.getUserItems(1, 0, 2, itemMapper);
        assertEquals(itemExtraInfoDtos, List.of(extraInfoDto));

        verify(userRepository).findById(1L);
        verify(pageableUtility).getPageableFromArguments(0, 2);
        verify(itemRepository).findByOwnerOrderById(simpleUser, Pageable.unpaged());
        verify(commentRepository).findByItemIn(List.of(simpleItem));
        verify(bookingRepository).findByItemInAndStatusOrderByItem(List.of(simpleItem), BookingStatus.APPROVED);
        verify(itemMapper).toDto(simpleItem, null, simpleBooking, List.of(simpleComment));
    }

    @Test
    public void testSearchItems_Success() {
        when(pageableUtility.getPageableFromArguments(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Pageable.unpaged());
        when(itemRepository.searchByNameOrDescription(Mockito.anyString(), Mockito.any(Pageable.class)))
                .thenReturn(List.of(simpleItem));

        assertEquals(List.of(simpleItem), itemService.searchItems(1, "asd", 0, 2));
        verify(pageableUtility).getPageableFromArguments(0, 2);
        verify(itemRepository).searchByNameOrDescription("asd", Pageable.unpaged());
    }

    @Test
    public void testAddComment_ItemDoesNotExist() {
        when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.addComment(simpleComment, 1, 1));
        verify(itemRepository).findById(1L);
    }

    @Test
    public void testAddComment_BookerDoesNotExist() {
        when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleItem));
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> itemService.addComment(simpleComment, 1, 1));
        verify(itemRepository).findById(1L);
        verify(userRepository).findById(1L);
    }

    @Test
    public void testAddComment_UserDidNotRentedItem() {
        when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleItem));
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleUser));
        when(bookingRepository.existsByItemAndBookerAndStatusAndEndBefore(
                Mockito.any(Item.class),
                Mockito.any(User.class),
                Mockito.any(BookingStatus.class),
                Mockito.any(LocalDateTime.class)
        )).thenReturn(false);

        assertThrows(ItemWasNotBeRentedException.class, () -> itemService.addComment(simpleComment, 1, 1));
        verify(itemRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(bookingRepository).existsByItemAndBookerAndStatusAndEndBefore(
                Mockito.any(Item.class),
                Mockito.any(User.class),
                Mockito.any(BookingStatus.class),
                Mockito.any(LocalDateTime.class)
        );
    }

    @Test
    public void testAddComment_Success() {
        when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleItem));
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleUser));
        when(bookingRepository.existsByItemAndBookerAndStatusAndEndBefore(
                Mockito.any(Item.class),
                Mockito.any(User.class),
                Mockito.any(BookingStatus.class),
                Mockito.any(LocalDateTime.class)
        )).thenReturn(true);
        when(commentRepository.save(Mockito.any(Comment.class)))
                .thenReturn(simpleComment);

        Comment comment = itemService.addComment(simpleComment, 1, 1);
        assertEquals(simpleComment, comment);

        verify(itemRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(bookingRepository).existsByItemAndBookerAndStatusAndEndBefore(
                Mockito.any(Item.class),
                Mockito.any(User.class),
                Mockito.any(BookingStatus.class),
                Mockito.any(LocalDateTime.class)
        );
        verify(commentRepository).save(simpleComment);
    }
}
