package ru.yandex.practicum.filmorate.storage.film.daoImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.genre.GenreNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.dao.GenreDao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


@Component("genreDbDao")
@Primary
@Slf4j
public class GenreDbDao implements GenreDao {
    private final JdbcTemplate jdbcTemplate;

    public GenreDbDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Genre getGenge(int id) {
        log.debug("Получен запрос на поиск жанра с id={}", id);
        String getGenreSql = "select genre_id,genre_name from genre where genre_id = ?;";
        Genre genre = jdbcTemplate.query(getGenreSql, (rs, rowNum) -> genreMapper(rs), id).stream().findAny().orElse(null);
        if (genre == null) {
            log.debug("Жанр с id={} не найден.", id);
            throw new GenreNotFoundException("Рейтинг MPA не найдены");
        }
        log.debug("Жанр с id={} найден.", id);
        return genre;
    }

    @Override
    public List<Genre> getGengesFilm(long filmId) {
        log.info("Получен запрос на чтение жанров для фильма с id={}", filmId);
        String getGenreSql = "SELECT g.GENRE_ID,g.GENRE_NAME FROM (SELECT * FROM FILMS_GENRE WHERE FILM_ID=?) fg " +
                "LEFT JOIN GENRE g ON fg.genre_id=g.GENRE_ID WHERE g.GENRE_ID IS NOT NULL;";
        List<Genre> listFilms = jdbcTemplate.query(getGenreSql, (rs, rowNum) -> genreMapper(rs), filmId);
        return listFilms;
    }

    @Override
    public List<Genre> getGenresFilms() {
        try {
            log.debug("Получен запрос на чтение всех жанров.");
            String getGenreSql = "select genre_id,genre_name from genre order by genre_id;";
            List<Genre> genres = jdbcTemplate.query(getGenreSql, (rs, rowNum) -> genreMapper(rs));
            return genres;
        } catch (Throwable e) {
            log.debug("Возникло исключение.");
            return null;
        }
    }

    @Override
    public void addFilmGenre(long filmId, int genreId) {
        try{
            jdbcTemplate.update("insert into films_genre(film_id,genre_id) VALUES(?,?);",filmId,genreId);
        } catch (RuntimeException e) {
            throw new GenreNotFoundException("Ошибка добавления фильму с filmId="+filmId+" жанра с genreId="+genreId);
        }
    }

    @Override
    public void delFilmGenre(long filmId) {
        try{
        jdbcTemplate.update("delete from films_genre where film_id=?;",filmId);
        } catch (RuntimeException e) {
            throw new GenreNotFoundException("Ошибка удаления жанров у фильма с filmId="+filmId);
        }
    }

    private Genre genreMapper(ResultSet rs) throws SQLException {
        int id = rs.getInt("GENRE_ID");
        String name = rs.getString("GENRE_NAME");
        log.debug("Считан жанр с id={}, name={}", id, name);
        return new Genre(id, name);
    }
}
