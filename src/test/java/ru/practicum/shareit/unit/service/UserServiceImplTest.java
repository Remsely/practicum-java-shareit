package ru.practicum.shareit.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User simpleUser;

    @BeforeEach
    public void setUp() {
        simpleUser = User.builder()
                .id(1L)
                .name("user")
                .email("user@user.com")
                .build();
    }

    @Test
    public void testAddUser_Success() {
        when(userRepository.save(Mockito.any(User.class)))
                .thenReturn(simpleUser);

        assertEquals(simpleUser, userService.addUser(simpleUser));
        verify(userRepository).save(simpleUser);
    }

    @Test
    public void testUpdateUser_UserDoesNotExists() {
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.updateUser(simpleUser, 1));
        verify(userRepository).findById(1L);
    }

    @Test
    public void testUpdateUser_Success() {
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleUser));
        when(userRepository.save(Mockito.any(User.class)))
                .thenReturn(simpleUser);

        assertEquals(simpleUser, userService.updateUser(simpleUser, 1));
        verify(userRepository).findById(1L);
        verify(userRepository).save(simpleUser);
    }

    @Test
    public void testUpdateUser_WithoutChangesSuccess() {
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleUser));
        when(userRepository.save(Mockito.any(User.class)))
                .thenReturn(simpleUser);

        assertEquals(simpleUser, userService.updateUser(User.builder().build(), 1));
        verify(userRepository).findById(1L);
        verify(userRepository).save(simpleUser);
    }

    @Test
    public void testDeleteUser_UserDoesNotExists() {
        when(userRepository.existsById(Mockito.anyLong()))
                .thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> userService.deleteUser(1));
        verify(userRepository).existsById(1L);
    }

    @Test
    public void testDeleteUser_Success() {
        when(userRepository.existsById(Mockito.anyLong()))
                .thenReturn(true);
        doNothing().when(userRepository).deleteById(Mockito.anyLong());

        userService.deleteUser(1);

        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    public void testGetUser_UserDoesNotExists() {
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.getUser(1));
        verify(userRepository).findById(1L);
    }

    @Test
    public void testGetUser_Success() {
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(simpleUser));

        assertEquals(simpleUser, userService.getUser(1));
        verify(userRepository).findById(1L);
    }

    @Test
    public void testGetUsers_Success() {
        when(userRepository.findAll())
                .thenReturn(List.of(simpleUser));

        assertEquals(List.of(simpleUser), userService.getUsers());
        verify(userRepository).findAll();
    }
}
