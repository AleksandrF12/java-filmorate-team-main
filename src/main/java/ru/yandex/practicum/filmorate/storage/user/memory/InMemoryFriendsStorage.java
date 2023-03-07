package ru.yandex.practicum.filmorate.storage.user.memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.dao.FriendsDao;
import ru.yandex.practicum.filmorate.storage.user.dao.UserDao;

import java.util.*;
import java.util.stream.Collectors;

@Component("friendsInMemoryDao")
@Slf4j
public class InMemoryFriendsStorage implements FriendsDao {

    private Map<Long, LinkedHashSet<User>> friends = new HashMap<>(); //информация о друзьях пользователя
    UserDao userDao;

    public InMemoryFriendsStorage(@Qualifier("userInMemoryDao") UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void addFriend(long userId, long friendId) {
        log.debug("Получен запрос на добавление пользователю с user_id={} друга с friend_id={}.", userId, friendId);
        Optional<LinkedHashSet<User>> friendSet = Optional.ofNullable(friends.get(userId));
        LinkedHashSet<User> newUser = new LinkedHashSet<>();
        if (friendSet.isPresent()) {
            newUser = friendSet.get();
            Optional<User> user = newUser.stream().filter(u -> u.getId() == friendId).findFirst();
            if (user.isPresent()) {
                return;
            }
        }
        log.debug("Пользователю с userId={} добавлен очередной друг с friendIв={}", userId, friendId);
        newUser.add(userDao.getUser(friendId));
        friends.put(userId, newUser);
    }

    @Override
    public void deleteFriend(long userId, long friendId) {
        log.debug("Получен запрос на удаление пользователю с user_id={} друга с friend_id={}.", userId, friendId);
        Optional<LinkedHashSet<User>> friendSet = Optional.ofNullable(friends.get(userId));
        if (friendSet.isPresent()) {
            Iterator<User> userIter = friendSet.get().iterator();
            while (userIter.hasNext()) {
                if (userIter.next().getId() == friendId) {
                    userIter.remove();
                    log.debug("Для пользователя userId={} удалён друг с friendId={}.", userId, friendId);
                }
            }
        }
    }

    @Override
    public List<User> getFriends(long userId) {
        log.debug("Запрос на получение друзей пользователя с userId={}", userId);
        Optional<Set<User>> friendSet = Optional.ofNullable(friends.get(userId));
        Set<User> newUser = new HashSet<>();
        if (!friendSet.isPresent()) {
            return new ArrayList<>();
        }
        return friendSet.get().stream().collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(long userId, long otherId) {
        log.debug("Получен запрос на поиск общих друзей для пользователей с userId={} и otherId={}.", userId, otherId);
        Optional<Set<User>> userFriendsOp = Optional.ofNullable(friends.get(userId));
        Optional<Set<User>> otherFriendsOp = Optional.ofNullable(friends.get(otherId));
        if (userFriendsOp.isPresent() && otherFriendsOp.isPresent()) {
            Set<Long> userFriends = userFriendsOp.get().stream().map(User::getId).collect(Collectors.toSet());
            Set<Long> otherFriends = otherFriendsOp.get().stream().map(User::getId).collect(Collectors.toSet());
            return userFriends.stream().filter(otherFriends::contains).map(userDao::getUser)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
