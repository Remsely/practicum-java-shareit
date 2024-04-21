package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.UserWithSuchEmailAlreadyExistException;
import ru.practicum.shareit.exception.model.ErrorResponse;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InMemoryUserStorage implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private long currentId = 1;

    @Override
    public User add(User user) {
        checkEmailExistence(user.getEmail());
        long id = currentId++;
        user.setId(id);
        users.put(id, user);
        return user;
    }

    @Override
    public User update(User user) {
        long id = user.getId();
        this.checkUserExistence(id);
        User savedUser = users.get(id);

        String email = user.getEmail();
        if (email != null && !email.equals(savedUser.getEmail())) {
            checkEmailExistence(email);
            savedUser.setEmail(email);
        }
        String name = user.getName();
        if (name != null) {
            savedUser.setName(name);
        }

        return savedUser;
    }

    @Override
    public void delete(long id) {
        this.checkUserExistence(id);
        users.remove(id);
    }

    @Override
    public User get(long id) {
        this.checkUserExistence(id);
        return users.get(id);
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void checkUserExistence(long id) {
        if (!users.containsKey(id)) {
            throw new EntityNotFoundException(ErrorResponse.builder()
                    .reason("User repository")
                    .message("User with id " + id + " does not exist!")
                    .build()
            );
        }
    }

    @Override
    public void checkEmailExistence(String email) {
        if (!emailIsUnique(email)) {
            throw new UserWithSuchEmailAlreadyExistException(ErrorResponse.builder()
                    .reason("User repository")
                    .message("User with email " + email + " already exist!")
                    .build()
            );
        }
    }

    private boolean emailIsUnique(String email) {
        return users.values().stream().noneMatch(user -> user.getEmail().equals(email));
    }
}
