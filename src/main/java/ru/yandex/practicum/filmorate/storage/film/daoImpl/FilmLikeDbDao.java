package ru.yandex.practicum.filmorate.storage.film.daoImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.dao.FilmLikeDao;
import ru.yandex.practicum.filmorate.storage.film.dao.GenreDao;
import ru.yandex.practicum.filmorate.storage.film.dao.MpaDao;

import java.util.*;

@Component
@Primary
@Slf4j
public class FilmLikeDbDao implements FilmLikeDao {

    private final JdbcTemplate jdbcTemplate;
    private final MpaDao mpaDao;
    private final GenreDao genreDao;
    private final FilmDbDao filmDbDao;

    public FilmLikeDbDao(JdbcTemplate jdbcTemplate
                        ,@Qualifier("mpaDbDao") MpaDao mpaDao
                        ,@Qualifier("genreDbDao") GenreDao genreDao
                        ,@Qualifier("filmDbStorage") FilmDbDao filmDbDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaDao = mpaDao;
        this.genreDao = genreDao;
        this.filmDbDao=filmDbDao;
    }

    //добавить лайки фильмам в таблицу films_like
    @Override
    public void addLike(long filmId, long userId) {
        try {
            String addSql = "insert into  FILMS_LIKE (film_id, user_id) select ?, ? from dual where not exists " +
                    "(select 1 from FILMS_LIKE where film_id=? AND user_id=?);";
            Object[] args = new Object[]{filmId, userId, filmId, userId};
            int addRow = jdbcTemplate.update(addSql, args);
            if (addRow <= 0) {
                log.debug("Ошибка добавления для фильма с id={} лайка от пользователя с id={}.", filmId, userId);
                throw new FilmNotFoundException("Фильм с id=" + filmId + " или пользователь с id=" + userId + " не найден.");
            }
        } catch (RuntimeException e) {
            log.debug("Возникло исключение: фильм или пользователь не найдены.");
            throw new FilmNotFoundException("Фильм с id=" + filmId + " или пользователь с id=" + userId + " не найден.");
        }
        log.debug("Для фильма с id={} добавлен лайк пользователем с id={}.", filmId, userId);
    }

    //удалить лайки фильмам из таблицы films_like
    @Override
    public void deleteLike(long filmId, long userId) {
        try {
            String delSql = "delete from  FILMS_LIKE where film_id=? AND user_id=?;";
            Object[] args = new Object[]{filmId, userId};
            int delRow = jdbcTemplate.update(delSql, args);
            if (delRow <= 0) {
                log.debug("Ошибка удаления для фильма с id={} лайка от пользователя с id={}.", filmId, userId);
                throw new FilmNotFoundException("Фильм с id=" + filmId + " или пользователь с id=" + userId + " не найден.");
            }
        } catch (RuntimeException e) {
            log.debug("Возникло исключение: фильм или пользователь не найдены.");
            throw new FilmNotFoundException("Фильм с id=" + filmId + " или пользователь с id=" + userId + " не найден.");
        }
        log.debug("Для фильма с id={} удалён лайк пользователем с id={}.", filmId, userId);
    }

    @Override
    public List<Film> getRecomendationFilm(long userId) {
        String getSql = "SELECT a.film_id,count(fl.FILM_ID) rating\n" +
                "FROM (SELECT DISTINCT fl.film_id\n" +
                "FROM (\n" +
                "SELECT gcf.user_id\n" +
                "FROM (SELECT ccf.user_id\n" +
                ",ccf.count_films\n" +
                ",RANK() OVER(ORDER BY count_films desc) AS group_num\n" +
                "FROM (\n" +
                "SELECT fl.USER_ID\n" +
                ",count(*) AS count_films\n" +
                "FROM (SELECT FILM_ID FROM films_like WHERE USER_ID =?) AS US\n" +
                "INNER JOIN \n" +
                "films_like AS FL ON us.film_id=fl.FILM_ID\n" +
                "WHERE fl.USER_ID <>?\n" +
                "GROUP BY fl.USER_ID ) ccf\n" +
                ") gcf\n" +
                "WHERE gcf.group_num=1\n" +
                ") ou\n" +
                "LEFT JOIN films_like fl ON ou.user_id=fl.user_id\n" +
                "LEFT JOIN (SELECT FILM_ID FROM films_like WHERE USER_ID =?) us\n" +
                "ON fl.film_id=us.film_id \n" +
                "WHERE us.film_id IS NULL ) a\n" +
                "LEFT JOIN FILMS_LIKE fl ON a.film_id=fl.FILM_ID \n" +
                "GROUP BY fl.FILM_ID\n";
        Object[] args = new Object[]{userId,userId,userId};
        return filmDbDao.getFilmsRatingSort(getSql,args);
    }
}
