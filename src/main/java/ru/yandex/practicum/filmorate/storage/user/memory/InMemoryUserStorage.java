package ru.yandex.practicum.filmorate.storage.user.memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.user.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.dao.UserDao;

import java.util.*;
import java.util.stream.Collectors;

//реализация методов добавления, удаления и модификации объектов.
@Component("userInMemoryDao")
@Slf4j
public class InMemoryUserStorage implements UserDao {
    private long maxId = 0;

    private Map<Long, User> users = new HashMap<>(); //информация о пользователях

    @Override
    public User addUser(User user) {
        log.debug("Получен запрос на добавление пользователя в InMemory...");
        String name = user.getName();
        String login = user.getLogin();
        if (name == null || name.isBlank()) {
            user.setName(login);
        }
        final long id = generateId();
        user.setId(id);
        this.users.put(id, user);
        //возвращаем информацию о добавленном пользователе
        log.debug("Добавлен пользователь с id={}, name={}, email={}, login={}, birthday={}"
                , id, user.getName(), user.getEmail(), user.getLogin(), user.getBirthday());
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (this.users.containsKey(user.getId())) {
            String name = user.getName();
            String login = user.getLogin();
            if (name == null || name.isBlank()) {
                user.setName(login);
            }
            this.users.put(user.getId(), user);
            log.info("Обновлены данные пользователя с id={}, name={}, email={}, login={}, birthday={}"
                    , user.getId(), user.getName(), user.getEmail(), user.getLogin(), user.getBirthday());
            return user;
        }
        throw new UserNotFoundException("Пользователь с id=" + user.getId() + " не найден.");
    }

    @Override
    public Set<User> getUsers() {
        log.info("Получен список пользователей.");
        return this.users.values().stream().collect(Collectors.toSet());
    }

    //возвращает данные о пользователе
    @Override
    public User getUser(long userId) {
        log.debug("Получен запрос из InMemory на пользователя с id={}",userId);
        if(!users.containsKey(userId)) {
            throw new UserNotFoundException("Пользователь с id="+userId+" не найден.");
        }
        return users.get(userId);
    }

    private long generateId() {
        return ++maxId;
    }

}
