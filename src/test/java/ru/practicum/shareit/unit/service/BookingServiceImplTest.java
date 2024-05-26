package ru.practicum.shareit.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.controller.BookingState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingJpaRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.common.utils.PageableUtility;
import ru.practicum.shareit.exception.AlreadyApprovedException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.UnavailableItemException;
import ru.practicum.shareit.exception.UserWithoutAccessRightsException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemJpaRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserJpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {
    @Mock
    private BookingJpaRepository bookingRepository;

    @Mock
    private UserJpaRepository userRepository;

    @Mock
    private ItemJpaRepository itemRepository;

    @Mock
    private PageableUtility pageableUtility;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private Booking simpleBooking;

    private User simpleUser;

    private User otherOwner;

    private Item simpleItem;

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
    }

    @Test
    public void testAddBooking_UserDoesNotExists() {
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookingService.addBooking(simpleBooking, 1));
        verify(userRepository).findById(1L);
    }

    @Test
    public void testAddBooking_ItemDoesNotExists() {
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleUser));
        when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookingService.addBooking(simpleBooking, 1));
        verify(userRepository).findById(1L);
        verify(itemRepository).findById(1L);
    }

    @Test
    public void testAddBooking_BookerIsOwner() {
        simpleItem.setOwner(simpleUser);

        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleUser));

        when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleItem));

        assertThrows(UserWithoutAccessRightsException.class, () -> bookingService.addBooking(simpleBooking, 1));
        verify(userRepository).findById(1L);
        verify(itemRepository).findById(1L);
    }

    @Test
    public void testAddBooking_ItemIsNotAvailable() {
        simpleItem.setOwner(otherOwner);
        simpleItem.setAvailable(false);

        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleUser));
        when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleItem));

        assertThrows(UnavailableItemException.class, () -> bookingService.addBooking(simpleBooking, 1));
        verify(userRepository).findById(1L);
        verify(itemRepository).findById(1L);
    }

    @Test
    public void testAddBooking_Success() {
        simpleItem.setOwner(otherOwner);
        simpleItem.setAvailable(true);

        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleUser));
        when(itemRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleItem));
        when(bookingRepository.save(Mockito.any(Booking.class)))
                .thenReturn(simpleBooking);

        Booking booking = bookingService.addBooking(simpleBooking, 1);

        assertEquals(booking.getStatus(), BookingStatus.WAITING);
        assertEquals(booking.getBooker(), simpleUser);
        assertEquals(booking.getItem(), simpleItem);
        assertEquals(booking.getEnd(), simpleBooking.getEnd());
        assertEquals(booking.getStart(), simpleBooking.getStart());

        verify(userRepository).findById(1L);
        verify(itemRepository).findById(1L);
        verify(bookingRepository).save(simpleBooking);
    }

    @Test
    public void testApproveBooking_BookingDoesNotExists() {
        when(bookingRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookingService.approveBooking(2, 1, true));
        verify(bookingRepository).findById(2L);
    }

    @Test
    public void testApproveBooking_UserIsNotOwner() {
        simpleBooking.getItem().setOwner(otherOwner);

        when(bookingRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleBooking));

        assertThrows(UserWithoutAccessRightsException.class, () -> bookingService.approveBooking(1, 1, true));
        verify(bookingRepository).findById(1L);
    }

    @Test
    public void testApproveBooking_BookingIsNotWaiting() {
        simpleBooking.getItem().setOwner(simpleUser);
        simpleBooking.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleBooking));

        assertThrows(AlreadyApprovedException.class, () -> bookingService.approveBooking(1, 1, true));
        verify(bookingRepository).findById(1L);
    }

    @Test
    public void testApproveBooking_ApprovedSuccess() {
        simpleBooking.getItem().setOwner(simpleUser);
        simpleBooking.setStatus(BookingStatus.WAITING);

        when(bookingRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleBooking));
        when(bookingRepository.save(Mockito.any(Booking.class)))
                .thenReturn(simpleBooking);

        Booking booking = bookingService.approveBooking(1, 1, true);

        assertEquals(booking.getStatus(), BookingStatus.APPROVED);
        verify(bookingRepository).findById(1L);
    }

    @Test
    public void testApproveBooking_RejectedSuccess() {
        simpleBooking.getItem().setOwner(simpleUser);
        simpleBooking.setStatus(BookingStatus.WAITING);

        when(bookingRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleBooking));
        when(bookingRepository.save(Mockito.any(Booking.class)))
                .thenReturn(simpleBooking);

        Booking booking = bookingService.approveBooking(1, 1, false);

        assertEquals(booking.getStatus(), BookingStatus.REJECTED);
        verify(bookingRepository).findById(1L);
    }

    @Test
    public void testGetBookingById_BookingDoesNotExist() {
        when(bookingRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookingService.getBookingById(2, 1));
        verify(bookingRepository).findById(2L);
    }

    @Test
    public void testGetBookingById_UserWithoutAccessRights() {
        simpleBooking.setBooker(otherOwner);
        simpleBooking.getItem().setOwner(otherOwner);

        when(bookingRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleBooking));

        assertThrows(UserWithoutAccessRightsException.class, () -> bookingService.getBookingById(1, 1));
        verify(bookingRepository).findById(1L);
    }

    @Test
    public void testGetBookingById_Success() {
        simpleBooking.setBooker(simpleUser);
        simpleBooking.getItem().setOwner(otherOwner);

        when(bookingRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleBooking));

        Booking booking = bookingService.getBookingById(1, 1);

        assertEquals(booking, simpleBooking);
        verify(bookingRepository).findById(1L);
    }

    @Test
    public void testGetUserBookings_BookerDoesNotExist() {
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookingService.getUserBookings(
                1, BookingState.ALL, 0, 2)
        );
        verify(userRepository).findById(1L);
    }

    @Test
    public void testGetUserBookings_AllSuccess() {
        List<Booking> bookings = List.of(simpleBooking);

        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleUser));
        when(pageableUtility.getPageableFromArguments(Mockito.isNull(), Mockito.isNull()))
                .thenReturn(Pageable.unpaged());
        when(bookingRepository.findBookingsByBookerOrderByStartDesc(
                Mockito.any(User.class), Mockito.any(Pageable.class))
        ).thenReturn(bookings);

        assertEquals(bookings, bookingService.getUserBookings(1, BookingState.ALL, null, null));

        verify(userRepository).findById(1L);
        verify(pageableUtility).getPageableFromArguments(null, null);
        verify(bookingRepository).findBookingsByBookerOrderByStartDesc(simpleUser, Pageable.unpaged());
    }

    @Test
    public void testGetUserBookings_CurrentSuccess() {
        List<Booking> bookings = List.of(simpleBooking);

        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleUser));
        when(pageableUtility.getPageableFromArguments(Mockito.isNull(), Mockito.isNull()))
                .thenReturn(Pageable.unpaged());
        when(bookingRepository.findBookingsByBookerAndStartBeforeAndEndAfterOrderByStartDesc(
                Mockito.any(User.class),
                Mockito.any(LocalDateTime.class),
                Mockito.any(LocalDateTime.class),
                Mockito.any(Pageable.class)
        )).thenReturn(bookings);

        assertEquals(bookings, bookingService.getUserBookings(1, BookingState.CURRENT, null, null));

        verify(userRepository).findById(1L);
        verify(pageableUtility).getPageableFromArguments(null, null);
        verify(bookingRepository).findBookingsByBookerAndStartBeforeAndEndAfterOrderByStartDesc(
                Mockito.any(User.class),
                Mockito.any(LocalDateTime.class),
                Mockito.any(LocalDateTime.class),
                Mockito.any(Pageable.class)
        );
    }

    @Test
    public void testGetUserBookings_PastSuccess() {
        List<Booking> bookings = List.of(simpleBooking);

        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleUser));
        when(pageableUtility.getPageableFromArguments(Mockito.isNull(), Mockito.isNull()))
                .thenReturn(Pageable.unpaged());
        when(bookingRepository.findBookingsByBookerAndEndBeforeOrderByStartDesc(
                Mockito.any(User.class),
                Mockito.any(LocalDateTime.class),
                Mockito.any(Pageable.class)
        )).thenReturn(bookings);

        assertEquals(bookings, bookingService.getUserBookings(1, BookingState.PAST, null, null));

        verify(userRepository).findById(1L);
        verify(pageableUtility).getPageableFromArguments(null, null);
        verify(bookingRepository).findBookingsByBookerAndEndBeforeOrderByStartDesc(
                Mockito.any(User.class),
                Mockito.any(LocalDateTime.class),
                Mockito.any(Pageable.class)
        );
    }

    @Test
    public void testGetUserBookings_FutureSuccess() {
        List<Booking> bookings = List.of(simpleBooking);

        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleUser));
        when(pageableUtility.getPageableFromArguments(Mockito.isNull(), Mockito.isNull()))
                .thenReturn(Pageable.unpaged());
        when(bookingRepository.findBookingsByBookerAndStartAfterOrderByStartDesc(
                Mockito.any(User.class),
                Mockito.any(LocalDateTime.class),
                Mockito.any(Pageable.class)
        )).thenReturn(bookings);

        assertEquals(bookings, bookingService.getUserBookings(1, BookingState.FUTURE, null, null));

        verify(userRepository).findById(1L);
        verify(pageableUtility).getPageableFromArguments(null, null);
        verify(bookingRepository).findBookingsByBookerAndStartAfterOrderByStartDesc(
                Mockito.any(User.class),
                Mockito.any(LocalDateTime.class),
                Mockito.any(Pageable.class)
        );
    }

    @Test
    public void testGetUserBookings_WaitingSuccess() {
        List<Booking> bookings = List.of(simpleBooking);

        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleUser));
        when(pageableUtility.getPageableFromArguments(Mockito.isNull(), Mockito.isNull()))
                .thenReturn(Pageable.unpaged());
        when(bookingRepository.findBookingsByBookerAndStatusOrderByStartDesc(
                Mockito.any(User.class),
                Mockito.any(BookingStatus.class),
                Mockito.any(Pageable.class)
        )).thenReturn(bookings);

        assertEquals(bookings, bookingService.getUserBookings(1, BookingState.WAITING, null, null));

        verify(userRepository).findById(1L);
        verify(pageableUtility).getPageableFromArguments(null, null);
        verify(bookingRepository).findBookingsByBookerAndStatusOrderByStartDesc(
                simpleUser,
                BookingStatus.WAITING,
                Pageable.unpaged()
        );
    }

    @Test
    public void testGetUserBookings_RejectedSuccess() {
        List<Booking> bookings = List.of(simpleBooking);

        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleUser));
        when(pageableUtility.getPageableFromArguments(Mockito.isNull(), Mockito.isNull()))
                .thenReturn(Pageable.unpaged());
        when(bookingRepository.findBookingsByBookerAndStatusOrderByStartDesc(
                Mockito.any(User.class),
                Mockito.any(BookingStatus.class),
                Mockito.any(Pageable.class)
        )).thenReturn(bookings);

        assertEquals(bookings, bookingService.getUserBookings(1, BookingState.REJECTED, null, null));

        verify(userRepository).findById(1L);
        verify(pageableUtility).getPageableFromArguments(null, null);
        verify(bookingRepository).findBookingsByBookerAndStatusOrderByStartDesc(
                simpleUser,
                BookingStatus.REJECTED,
                Pageable.unpaged()
        );
    }

    @Test
    public void testGetUserItemsBookings_BookerDoesNotExist() {
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookingService.getUserItemsBookings(
                1, BookingState.ALL, 0, 2)
        );
        verify(userRepository).findById(1L);
    }

    @Test
    public void testGetUserItemsBookings_AllSuccess() {
        List<Booking> bookings = List.of(simpleBooking);

        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleUser));
        when(pageableUtility.getPageableFromArguments(Mockito.isNull(), Mockito.isNull()))
                .thenReturn(Pageable.unpaged());
        when(bookingRepository.findBookingsByItemOwner(
                Mockito.any(User.class), Mockito.any(Pageable.class))
        ).thenReturn(bookings);

        assertEquals(bookings, bookingService.getUserItemsBookings(1, BookingState.ALL, null, null));

        verify(userRepository).findById(1L);
        verify(pageableUtility).getPageableFromArguments(null, null);
        verify(bookingRepository).findBookingsByItemOwner(simpleUser, Pageable.unpaged());
    }

    @Test
    public void testGetUserItemsBookings_CurrentSuccess() {
        List<Booking> bookings = List.of(simpleBooking);

        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleUser));
        when(pageableUtility.getPageableFromArguments(Mockito.isNull(), Mockito.isNull()))
                .thenReturn(Pageable.unpaged());
        when(bookingRepository.findCurrentBookingsByItemOwner(
                Mockito.any(User.class),
                Mockito.any(LocalDateTime.class),
                Mockito.any(Pageable.class)
        )).thenReturn(bookings);

        assertEquals(bookings, bookingService.getUserItemsBookings(1, BookingState.CURRENT, null, null));

        verify(userRepository).findById(1L);
        verify(pageableUtility).getPageableFromArguments(null, null);
        verify(bookingRepository).findCurrentBookingsByItemOwner(
                Mockito.any(User.class),
                Mockito.any(LocalDateTime.class),
                Mockito.any(Pageable.class)
        );
    }

    @Test
    public void testGetUserItemsBookings_PastSuccess() {
        List<Booking> bookings = List.of(simpleBooking);

        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleUser));
        when(pageableUtility.getPageableFromArguments(Mockito.isNull(), Mockito.isNull()))
                .thenReturn(Pageable.unpaged());
        when(bookingRepository.findPastBookingsByItemOwner(
                Mockito.any(User.class),
                Mockito.any(LocalDateTime.class),
                Mockito.any(Pageable.class)
        )).thenReturn(bookings);

        assertEquals(bookings, bookingService.getUserItemsBookings(1, BookingState.PAST, null, null));

        verify(userRepository).findById(1L);
        verify(pageableUtility).getPageableFromArguments(null, null);
        verify(bookingRepository).findPastBookingsByItemOwner(
                Mockito.any(User.class),
                Mockito.any(LocalDateTime.class),
                Mockito.any(Pageable.class)
        );
    }

    @Test
    public void testGetUserItemsBookings_FutureSuccess() {
        List<Booking> bookings = List.of(simpleBooking);

        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleUser));
        when(pageableUtility.getPageableFromArguments(Mockito.isNull(), Mockito.isNull()))
                .thenReturn(Pageable.unpaged());
        when(bookingRepository.findFutureBookingsByItemOwner(
                Mockito.any(User.class),
                Mockito.any(LocalDateTime.class),
                Mockito.any(Pageable.class)
        )).thenReturn(bookings);

        assertEquals(bookings, bookingService.getUserItemsBookings(1, BookingState.FUTURE, null, null));

        verify(userRepository).findById(1L);
        verify(pageableUtility).getPageableFromArguments(null, null);
        verify(bookingRepository).findFutureBookingsByItemOwner(
                Mockito.any(User.class),
                Mockito.any(LocalDateTime.class),
                Mockito.any(Pageable.class)
        );
    }

    @Test
    public void testGetUserItemsBookings_WaitingSuccess() {
        List<Booking> bookings = List.of(simpleBooking);

        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleUser));
        when(pageableUtility.getPageableFromArguments(Mockito.isNull(), Mockito.isNull()))
                .thenReturn(Pageable.unpaged());
        when(bookingRepository.findBookingsByItemOwnerAndStatus(
                Mockito.any(User.class),
                Mockito.any(BookingStatus.class),
                Mockito.any(Pageable.class)
        )).thenReturn(bookings);

        assertEquals(bookings, bookingService.getUserItemsBookings(1, BookingState.WAITING, null, null));

        verify(userRepository).findById(1L);
        verify(pageableUtility).getPageableFromArguments(null, null);
        verify(bookingRepository).findBookingsByItemOwnerAndStatus(
                simpleUser,
                BookingStatus.WAITING,
                Pageable.unpaged()
        );
    }

    @Test
    public void testGetUserItemsBookings_RejectedSuccess() {
        List<Booking> bookings = List.of(simpleBooking);

        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleUser));
        when(pageableUtility.getPageableFromArguments(Mockito.isNull(), Mockito.isNull()))
                .thenReturn(Pageable.unpaged());
        when(bookingRepository.findBookingsByItemOwnerAndStatus(
                Mockito.any(User.class),
                Mockito.any(BookingStatus.class),
                Mockito.any(Pageable.class)
        )).thenReturn(bookings);

        assertEquals(bookings, bookingService.getUserItemsBookings(1, BookingState.REJECTED, null, null));

        verify(userRepository).findById(1L);
        verify(pageableUtility).getPageableFromArguments(null, null);
        verify(bookingRepository).findBookingsByItemOwnerAndStatus(
                simpleUser,
                BookingStatus.REJECTED,
                Pageable.unpaged()
        );
    }
}
