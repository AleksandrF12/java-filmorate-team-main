package ru.yandex.practicum.filmorate.storage.user.daoImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.dao.FriendsDao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@Qualifier("friendsDbDao")
@Primary
@Slf4j
public class FriendsDbDao implements FriendsDao {

    private final JdbcTemplate jdbcTemplate;

    public FriendsDbDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addFriend(long userId, long friendId) {
        //считываем из таблицы friends
        String friendsSql = "select user_id, friend_id, friend_status from friends where (user_id=? and friend_id=?) " +
                "or (user_id=? and friend_id=?);";
        Object[] args = new Object[]{userId, friendId, friendId, userId};
        SqlRowSet friendsRows = jdbcTemplate.queryForRowSet(friendsSql, args);
        if (friendsRows.first()) {
            int user1 = friendsRows.getInt("user_id");
            boolean status = friendsRows.getBoolean("friend_status");
            //если найдена запись, то смотрим на friend_status, если он true,то пользователи дружат между собой
            //тогда просто выходим
            //если friend_status=false, то user_id дружит с friend_id, но не наоборот
            //если user_id=userId, то просто выходим
            //если user_id=friendId и friend_status=true, то выходим
            if (status || user1 == userId) {
                return;
            } else if (user1 == friendId & !status) {
                //если user_id=friendId и friend_status=false, то отправляем запрос на friend_status=true (подтверждаем дружбу)
                String friendSqlTrue = "UPDATE friends SET user_id=?,friend_id=?,friend_status=?;";
                args = new Object[]{friendId, userId, true};
                jdbcTemplate.update(friendSqlTrue, args);
            }
        } else {
            //если запись не найдена, то добавляем её
            String addFriendSql = "INSERT INTO friends(user_id,friend_id) VALUES(?,?);";
            args = new Object[]{userId, friendId};
            jdbcTemplate.update(addFriendSql, args);
        }
    }

    @Override
    public void deleteFriend(long userId, long friendId) {
        //userId удаляет из друзей friendId
        //возможно в след. случаях:
        //существует запись: userId, friendId, false - неподтверждённая дружба - просто удаляем
        //существует запись: friendId, userId, false - неподтверждённая дружба - ничего не делаем
        //существует запись: userId, friendId, true - подтверждённая дружба - удаляем и записываем friendId, userId, false
        //существует запись: friendId, userId, true - подтверждённая дружба - удаляем и записываем friendId, userId, false
        //если запись не найдена, то ничего не делаем.
        //считываем из таблицы friends
        String friendsSql = "select user_id, friend_id, friend_status from friends where (user_id=? and friend_id=?) " +
                "or (user_id=? and friend_id=?);";
        Object[] args = new Object[]{userId, friendId, friendId, userId};
        SqlRowSet friendsRows = jdbcTemplate.queryForRowSet(friendsSql, args);
        if (friendsRows.first()) {
            log.debug("Получен непустой ответ на удаление от сервера...");
            boolean status = friendsRows.getBoolean("friend_status");
            log.debug("Статус дружбы: обоюдная...");
            String delFriendSql = "DELETE FROM friends WHERE (user_id=? and friend_id=?) OR (user_id=? and friend_id=?);";
            args = new Object[]{userId, friendId, friendId, userId};
            jdbcTemplate.update(delFriendSql, args);
            if (status) {
                String addFriendSql = "INSERT INTO friends(user_id,friend_id) VALUES(?,?);";
                args = new Object[]{friendId, userId};
                jdbcTemplate.update(addFriendSql, args);
            }
        }
    }

    @Override
    public List<User> getFriends(long userId) {
        //возвращаем друзей пользователя userId
        //посмотреть, возможно переписать
        String getFriendsSql = "SELECT u2.* FROM USERS u2 LEFT JOIN (SELECT DISTINCT CASE WHEN "+
                "(f.FRIEND_ID =? AND f.FRIEND_STATUS) THEN f.USER_ID ELSE f.FRIEND_ID END AS FRIEND_ID " +
                "FROM FRIENDS f WHERE f.USER_ID =? OR (f.FRIEND_ID =? AND f.FRIEND_STATUS)) fr " +
                "ON u2.USER_ID =fr.FRIEND_ID WHERE fr.FRIEND_ID IS NOT NULL;";
        Object[] args=new Object[]{userId,userId,userId};
        List<User> users=jdbcTemplate.query(getFriendsSql, (rs, rowNum) ->userMapper(rs),args);
        log.debug("Количество друзей пользователя с id={}: {}",userId,users.size());
        return users;
    }

    @Override
    public List<User> getCommonFriends(long userId, long otherId) {
        String commonFriendSql="SELECT * FROM USERS WHERE USER_ID IN (SELECT f1.friend FROM (SELECT DISTINCT CASE WHEN (u.USER_ID =f.FRIEND_ID AND "+
                "f.FRIEND_STATUS) THEN f.USER_ID ELSE f.FRIEND_ID END AS friend FROM (SELECT * FROM USERS  WHERE USER_ID =?) u LEFT JOIN "+
                "FRIENDS f ON u.USER_ID =f.USER_ID OR (u.USER_ID =f.FRIEND_ID AND f.FRIEND_STATUS) "+
                ") f1 INNER JOIN (SELECT DISTINCT CASE WHEN (u.USER_ID =f.FRIEND_ID AND "+
                "f.FRIEND_STATUS) THEN f.USER_ID ELSE f.FRIEND_ID END AS friend FROM (SELECT * FROM USERS  WHERE USER_ID =?) u LEFT JOIN FRIENDS f " +
                "ON u.USER_ID =f.USER_ID OR (u.USER_ID =f.FRIEND_ID AND f.FRIEND_STATUS)) f2 " +
                "ON f1.friend=f2.friend);";
        Object[] args=new Object[]{userId,otherId};
        List<User> users=jdbcTemplate.query(commonFriendSql, (rs, rowNum) ->userMapper(rs),args);
        log.debug("Количество общих друзей пользователей с id={},{}: {}",userId,otherId,users.size());
        return users;
    }

    private User userMapper(ResultSet rs) throws SQLException {
        //перебираем записи результирующего набора
        return new User(rs.getLong("user_id"),
                rs.getString("email"),
                rs.getString("login"),
                rs.getString("name"),
                rs.getDate("birthday").toLocalDate());
    }
}
