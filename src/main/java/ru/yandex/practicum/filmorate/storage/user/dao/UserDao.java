package ru.yandex.practicum.filmorate.storage.user.dao;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Set;

//методы добавления, удаления и модификации объектов.
public interface UserDao {
    User addUser(User user);

    User updateUser(User user);

    List<User> getUsers();

    void deleteUser(long userId);

    User getUser(long userId);
}
