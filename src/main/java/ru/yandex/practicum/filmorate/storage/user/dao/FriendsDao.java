package ru.yandex.practicum.filmorate.storage.user.dao;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface FriendsDao {
    void addFriend(long userId, long friendId); //пользователь userId добавляет в друзья пользователя friendId
    void deleteFriend(long userId, long friendId); //удаление пользователем друга friendId
    List<User> getFriends(long userId); //возвращение списка друзей пользователя userId
    List<User> getCommonFriends(long userId, long otherId); //возвращение общих друзей пользователя
}
