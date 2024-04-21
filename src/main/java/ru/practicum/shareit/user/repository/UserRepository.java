package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserRepository {
    User add(User user);

    User update(User user);

    void delete(long id);

    User get(long id);

    List<User> getAll();

    void checkUserExistence(long id);

    void checkEmailExistence(String email);
}
