package ru.yandex.practicum.filmorate.storage.film.daoImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.storage.film.dao.FilmLikeDao;
import ru.yandex.practicum.filmorate.storage.film.dao.GenreDao;
import ru.yandex.practicum.filmorate.storage.film.dao.MpaDao;

@Component
@Primary
@Slf4j
public class FilmLikeDbDao implements FilmLikeDao {

    private final JdbcTemplate jdbcTemplate;
    private final MpaDao mpaDao;
    private final GenreDao genreDao;

    public FilmLikeDbDao(JdbcTemplate jdbcTemplate, @Qualifier("mpaDbDao") MpaDao mpaDao,
                         @Qualifier("genreDbDao") GenreDao genreDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaDao = mpaDao;
        this.genreDao = genreDao;
    }

    //добавить лайки фильмам в таблицу films_like
    @Override
    public void addLike(long filmId, long userId) {
        try{
            String addSql="insert into  FILMS_LIKE (film_id, user_id) select ?, ? from dual where not exists "+
                    "(select 1 from FILMS_LIKE where film_id=? AND user_id=?);";
            Object[] args = new Object[] {filmId,userId,filmId,userId};
            int addRow=jdbcTemplate.update(addSql, args);
            if (addRow<=0) {
                log.debug("Ошибка добавления для фильма с id={} лайка от пользователя с id={}.",filmId,userId);
                throw new FilmNotFoundException("Фильм с id="+filmId+" или пользователь с id="+userId+" не найден.");
            }
        } catch (RuntimeException e) {
            log.debug("Возникло исключение: фильм или пользователь не найдены.");
            throw new FilmNotFoundException("Фильм с id="+filmId+" или пользователь с id="+userId+" не найден.");
        }
        log.debug("Для фильма с id={} добавлен лайк пользователем с id={}.",filmId,userId);
    }

    //удалить лайки фильмам из таблицы films_like
    @Override
    public void deleteLike(long filmId, long userId) {
        try{
            String delSql="delete from  FILMS_LIKE where film_id=? AND user_id=?;";
            Object[] args = new Object[] {filmId,userId};
            int delRow=jdbcTemplate.update(delSql, args);
            if (delRow<=0) {
                log.debug("Ошибка удаления для фильма с id={} лайка от пользователя с id={}.",filmId,userId);
                throw new FilmNotFoundException("Фильм с id="+filmId+" или пользователь с id="+userId+" не найден.");
            }
        } catch (RuntimeException e) {
            log.debug("Возникло исключение: фильм или пользователь не найдены.");
            throw new FilmNotFoundException("Фильм с id="+filmId+" или пользователь с id="+userId+" не найден.");
        }
        log.debug("Для фильма с id={} удалён лайк пользователем с id={}.",filmId,userId);
    }

}
