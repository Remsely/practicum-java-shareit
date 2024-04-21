package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;


    @Override
    public User addUser(User user) {
        User addedUser = userRepository.add(user);
        log.info("add User: a user with an id {} has been added. User : {}.", addedUser.getId(), addedUser);
        return addedUser;
    }

    @Override
    public User updateUser(User user, long id) {
        user.setId(id);
        User updatedUser = userRepository.update(user);
        log.info("update User: a user with an id {} has been updated. User : {}.", updatedUser.getId(), updatedUser);
        return updatedUser;
    }

    @Override
    public void deleteUser(long id) {
        userRepository.delete(id);
        log.info("delete User: a user with an id {} has been deleted.", id);
    }

    @Override
    public User getUser(long id) {
        User user = userRepository.get(id);
        log.info("get User: a user with an id {} has been received. User : {}.", id, user);
        return user;
    }

    @Override
    public List<User> getUsers() {
        List<User> users = userRepository.getAll();
        log.info("get Users: a users list has been received. List : {}", users);
        return users;
    }
}
