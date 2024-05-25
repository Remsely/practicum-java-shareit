package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.model.ErrorResponse;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserJpaRepository;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserJpaRepository userRepository;

    @Override
    public User addUser(User user) {
        User addedUser = userRepository.save(user);
        log.info("add User: a user with an id {} has been added. User : {}.", addedUser.getId(), addedUser);
        return addedUser;
    }

    @Override
    public User updateUser(User user, long id) {
        User userToUpdate = findUser(id);
        updateNonNullProperties(userToUpdate, user);
        User updatedUser = userRepository.save(userToUpdate);
        log.info("update User: a user with an id {} has been updated. User : {}.", updatedUser.getId(), updatedUser);
        return updatedUser;
    }

    @Transactional
    @Override
    public void deleteUser(long id) {
        checkUserExistence(id);
        userRepository.deleteById(id);
        log.info("delete User: a user with an id {} has been deleted.", id);
    }

    @Override
    public User getUser(long id) {
        User user = findUser(id);
        log.info("get User: a user with an id {} has been received. User : {}.", id, user);
        return user;
    }

    @Override
    public List<User> getUsers() {
        List<User> users = userRepository.findAll();
        log.info("get Users: a users list has been received. List (size = {}) : {}", users.size(), users);
        return users;
    }

    private User findUser(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorResponse.builder()
                                .reason("User repository")
                                .error("User with id " + id + " does not exist!")
                                .build()
                ));
    }

    private void updateNonNullProperties(User existingUser, User newUser) {
        final String newEmail = newUser.getEmail();
        if (newEmail != null && !newEmail.equals(existingUser.getEmail())) {
            existingUser.setEmail(newUser.getEmail());
        }
        if (newUser.getName() != null) {
            existingUser.setName(newUser.getName());
        }
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
}
