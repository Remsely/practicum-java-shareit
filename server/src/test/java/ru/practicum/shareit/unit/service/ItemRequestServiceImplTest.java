package ru.practicum.shareit.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.common.utils.PageableUtility;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceImplTest {
    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PageableUtility pageableUtility;

    @InjectMocks
    private ItemRequestServiceImpl requestService;

    private ItemRequest simpleRequest;

    private User simpleUser;

    @BeforeEach
    public void setUp() {
        simpleUser = User.builder()
                .id(1L)
                .name("user")
                .email("user@user.com")
                .build();

        simpleRequest = ItemRequest.builder()
                .id(1L)
                .description("description")
                .user(simpleUser)
                .build();
    }

    @Test
    public void testAddRequest_UserDoesNotExist() {
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> requestService.addRequest(simpleRequest, 1));
        verify(userRepository).findById(1L);
    }

    @Test
    public void testAddRequest_Success() {
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleUser));
        when(requestRepository.save(Mockito.any(ItemRequest.class)))
                .thenReturn(simpleRequest);

        ItemRequest request = requestService.addRequest(simpleRequest, 1);

        assertNotNull(request.getCreated());
        verify(userRepository).findById(1L);
        verify(requestRepository).save(simpleRequest);
    }

    @Test
    public void testGetRequest_UserDoesNotExist() {
        when(userRepository.existsById(Mockito.anyLong()))
                .thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> requestService.getRequest(1, 1));
        verify(userRepository).existsById(1L);
    }

    @Test
    public void testGetRequest_RequestDoesNotExist() {
        when(userRepository.existsById(Mockito.anyLong()))
                .thenReturn(true);
        when(requestRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> requestService.getRequest(1, 1));
        verify(userRepository).existsById(1L);
        verify(requestRepository).findById(1L);
    }

    @Test
    public void testGetRequest_Success() {
        when(userRepository.existsById(Mockito.anyLong()))
                .thenReturn(true);
        when(requestRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleRequest));

        ItemRequest request = requestService.getRequest(1, 1);

        assertEquals(request, simpleRequest);
        verify(userRepository).existsById(1L);
        verify(requestRepository).findById(1L);
    }

    @Test
    public void testGetUserRequests_UserDoesNotExist() {
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> requestService.getUserRequests(1));
        verify(userRepository).findById(1L);
    }

    @Test
    public void testGetUserRequests_Success() {
        List<ItemRequest> requests = List.of(simpleRequest, simpleRequest);

        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleUser));
        when(requestRepository.findByUser(Mockito.any(User.class)))
                .thenReturn(requests);

        assertEquals(requests, requestService.getUserRequests(1));
        verify(userRepository).findById(1L);
        verify(requestRepository).findByUser(simpleUser);
    }

    @Test
    public void testGetAllRequests_UserDoesNotExist() {
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> requestService.getAllRequests(0, 2, 2));
        verify(userRepository).findById(2L);
    }

    @Test
    public void testGetAllRequests_Success() {
        List<ItemRequest> requests = List.of(simpleRequest, simpleRequest);

        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleUser));
        when(pageableUtility.getPageableFromArguments(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Pageable.unpaged());
        when(requestRepository.findByUserNot(Mockito.any(User.class), Mockito.any(Pageable.class)))
                .thenReturn(requests);

        assertEquals(requests, requestService.getAllRequests(0, 2, 1));

        verify(userRepository).findById(1L);
        verify(pageableUtility).getPageableFromArguments(0, 2);
        verify(requestRepository).findByUserNot(simpleUser, Pageable.unpaged());
    }
}
