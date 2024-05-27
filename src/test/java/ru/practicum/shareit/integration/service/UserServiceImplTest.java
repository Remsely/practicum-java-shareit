package ru.practicum.shareit.integration.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImplTest {
    private final UserService userService;

    @Test
    public void testUpdateUser() {
        User user = userService.addUser(User.builder()
                .name("user")
                .email("email@email.com")
                .build()
        );

        assertThrows(EntityNotFoundException.class, () ->
                userService.updateUser(User.builder().build(), user.getId() + 1));

        User updatedUser = userService.updateUser(User.builder()
                .email("other.email@email.com")
                .build(), user.getId());

        assertThat(updatedUser.getEmail()).isNotEqualTo("email@email.com");
        assertThat(updatedUser.getEmail()).isEqualTo("other.email@email.com");
        assertThat(updatedUser.getId()).isEqualTo(user.getId());
        assertThat(updatedUser.getName()).isEqualTo("user");
    }
}
