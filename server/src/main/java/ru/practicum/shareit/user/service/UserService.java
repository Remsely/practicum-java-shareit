package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    User addUser(User user);

    User updateUser(User user, long id);

    void deleteUser(long id);

    User getUser(long id);

    List<User> getUsers();
}
