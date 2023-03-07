package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.user.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.dao.FriendsDao;
import ru.yandex.practicum.filmorate.storage.user.dao.UserDao;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

   private final UserDao userStorage;
   private final FriendsDao friendsDao;

    public UserService(UserDao userStorage,
                       FriendsDao friendsDao) {
        this.userStorage = userStorage;
        this.friendsDao = friendsDao;
    }

    //добавление пользователя
    public User addUser(User user) {
        log.info("Получен запрос на добавление пользователя...");
        return userStorage.addUser(user);
    }

    //обновление пользователя
    public User updateUser(User user) {
        log.info("Получен запрос на обновление пользователя...");
        isValidIdUser(user.getId());
        return userStorage.updateUser(user);
    }

    //возвращает информацию обо всех пользователях
    public Set<User> getUsers() {
        log.info("Получен запрос на чтение пользователей...");
        return userStorage.getUsers().stream().collect(Collectors.toSet());
    }

    //получение данных о пользователе
    public User getUser(long userId) {
        log.info("Получен запрос на получение данных пользователя с id={}", userId);
//        isValidIdUser(userId);
        log.info("Пользователь с id={} получен.", userId);
        return userStorage.getUser(userId);
    }

    //добавление в друзья
    public void addFriend(long userId, long friendId) {
        log.debug("Получен запрос на добавление для пользователя с id={} друга с id={}", userId, friendId);
        isValidIdUser(userId);
        isValidIdUser(friendId);
        isNotEqualIdUser(userId, friendId);
        //проверка наличия пользователей в БД
        userStorage.getUser(userId);
        userStorage.getUser(friendId);
        friendsDao.addFriend(userId,friendId);
        log.info("Для пользователя с id = {} добавлен друг с id={}", userId, friendId);
    }

    //удаление из друзей
    public void deleteFriend(long userId, long friendId) {
        log.debug("Получен запрос на удаление для пользователя с id={} друга с id={}", userId, friendId);
        isValidIdUser(userId);
        isValidIdUser(friendId);
        isNotEqualIdUser(userId, friendId);
        log.debug("Запрос на удаление для пользователя с id={} друга с id={} одобрен.", userId, friendId);
        friendsDao.deleteFriend(userId,friendId);
    }

    //возвращение списка друзей пользователя
    public List<User> getFriends(long userId) {
        log.debug("Получен запрос на получение для пользователя с id={} списка друзей", userId);
        isValidIdUser(userId);
        return friendsDao.getFriends(userId);
    }


    //список друзей, общих с другим пользователем.
    public List<User> getCommonFriends(long userId, long otherId) {
        log.debug("Получен запрос на поиск общих друзей для пользователей с userId={} и otherId={}.", userId, otherId);
        isValidIdUser(userId);
        isValidIdUser(otherId);
        isNotEqualIdUser(userId, otherId);
        return friendsDao.getCommonFriends(userId,otherId);
    }

    private boolean isValidIdUser(long userId) {
        if (userId <= 0) {
            throw new UserNotFoundException("Некорректный id=" + userId + " пользователя.");
        }
        log.debug("Валидация пользователя с id={} прошла успешно.", userId);
        return true;
    }

    //проверяет не равныли id пользователя и друга
    private boolean isNotEqualIdUser(long userId, long friendId) {
        if (userId == friendId) {
            throw new UserNotFoundException("Пользователь с id=" + userId + " не может добавить сам себя в друзья.");
        }
        return true;
    }
     public void deleteUserById (long id) {
         isValidIdUser(id);
         userStorage.deleteUser(id);
     }
}
