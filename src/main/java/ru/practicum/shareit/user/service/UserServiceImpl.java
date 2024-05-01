package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.UserAlreadyExistException;
import ru.practicum.shareit.exception.model.ErrorResponse;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserJpaRepository;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserJpaRepository userRepository;

    @Transactional
    @Override
    public User addUser(User user) {
        User addedUser;
        try {
            addedUser = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistException(ErrorResponse.builder()
                    .reason("User repository")
                    .error("User with email " + user.getEmail() + " already exist!")
                    .build()
            );
        }
        log.info("add User: a user with an id {} has been added. User : {}.", addedUser.getId(), addedUser);
        return addedUser;
    }

    @Transactional
    @Override
    public User updateUser(User user, long id) {
        User userToUpdate = findUser(id);
        updateNonNullProperties(userToUpdate, user);

        /*
        Здесь исключение почему-то не ловится. Я прочитал, что это исключение не выбрасывается внутри транзакции, и
        его можно поймать за ее пределами. Но в таком случае я не могу понять, почему оно ловится в addUser...

        Исключение ловится, если сделать так:

        @Override
        public User updateUser(User user, long id) {
            User userToUpdate = findUser(id);
            updateNonNullProperties(userToUpdate, user);

            User updatedUser;
            try {
                updatedUser = updateUser(userToUpdate);
                log.info("update User: a user with an id {} has been updated. User : {}.", updatedUser.getId(), updatedUser);
                return updatedUser;
            } catch (DataIntegrityViolationException e) {
                throw new UserAlreadyExistException(ErrorResponse.builder()
                        .reason("User repository")
                        .error("User with email " + user.getEmail() + " already exist!")
                        .build()
                );
            }
        }

        @Transactional
        public User updateUser(User userToUpdate) {
            return userRepository.save(userToUpdate);
        }

        Но мне не нравится такой метод. Даже IDEA ругается.
        Есть еще вариант ловить исключение в контроллере, но это как будто тоже не очень хорошо.
        Стоит ли пытаться ловить это исключение уже в ErrorHandler и писать там логику по проверке деталей ошибки
        (чтобы знать, что ее вызвал именно email), чтобы делать структурированный вывод?
        */

        User updatedUser;
        try {
            updatedUser = userRepository.save(userToUpdate);
            log.info("update User: a user with an id {} has been updated. User : {}.", updatedUser.getId(), updatedUser);
            return updatedUser;
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistException(ErrorResponse.builder()
                    .reason("User repository")
                    .error("User with email " + user.getEmail() + " already exist!")
                    .build()
            );
        }
    }

    @Transactional
    @Override
    public void deleteUser(long id) {
        checkUserExistence(id);
        userRepository.deleteById(id);
        log.info("delete User: a user with an id {} has been deleted.", id);
    }

    @Transactional(readOnly = true)
    @Override
    public User getUser(long id) {
        User user = findUser(id);
        log.info("get User: a user with an id {} has been received. User : {}.", id, user);
        return user;
    }

    @Transactional(readOnly = true)
    @Override
    public List<User> getUsers() {
        List<User> users = userRepository.findAll();
        log.info("get Users: a users list has been received. List : {}", users);
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
