package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/users")
@Qualifier("userInMemoryDao")
@Slf4j
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {

        this.userService = userService;
    }

    //добавление пользователя
    @PostMapping
    private User addUser(@Valid @RequestBody User user) {

        return userService.addUser(user);
    }

    //обновление пользователя
    @PutMapping
    private User updateUser(@Valid @RequestBody User user) {

        return  userService.updateUser(user);
    }

    //возвращает информацию обо всех пользователях
    @GetMapping
    private Set<User> getUsers() {

        return userService.getUsers();
    }

    //получение данных о пользователе
    @GetMapping("/{id}")
    private User getUser(@PathVariable("id") long userId) {

        return userService.getUser(userId);
    }

    //добавление в друзья
    @PutMapping("/{id}/friends/{friendId}")
    private void addFriend(@PathVariable("id") long userId, @PathVariable("friendId") long friendId) {
        userService.addFriend(userId, friendId);
    }

    //удаление из друзей
    @DeleteMapping("/{id}/friends/{friendId}")
    private void deleteFriend(@PathVariable("id") long userId, @PathVariable("friendId") long friendId) {
        userService.deleteFriend(userId, friendId);
    }

    //возвращение списка друзей пользователя
    @GetMapping("/{id}/friends")
    private List<User> getFriends(@PathVariable("id") long userId) {
        log.info("Получен запрос на получение для пользователя с id={} списка друзей", userId);
        return userService.getFriends(userId);
    }

    //список друзей, общих с другим пользователем.
    @GetMapping("/{id}/friends/common/{otherId}")
    private List<User> getOtherFriends(@PathVariable("id") long userId, @PathVariable("otherId") long otherId) {
        log.info("Получен запрос на поиск общих друзей для пользователей с userId={} и otherId={}.", userId, otherId);
        return userService.getCommonFriends(userId, otherId);
    }
    //удаление пользователя по id
    @DeleteMapping("/{id}")
    protected void deleteUser(@PathVariable("id") long Id) {
        log.info("Получен запрос на удаление пользователем: {}",Id);
        userService.deleteUserById(Id);
    }
}
