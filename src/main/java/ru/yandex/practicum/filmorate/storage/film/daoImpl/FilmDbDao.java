package ru.yandex.practicum.filmorate.storage.film.daoImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.film.dao.FilmDao;
import ru.yandex.practicum.filmorate.storage.film.dao.GenreDao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component("filmDbStorage")
@Primary
@Slf4j
public class FilmDbDao implements FilmDao {
    private final JdbcTemplate jdbcTemplate;
    private final GenreDao genreDao;

    public FilmDbDao(JdbcTemplate jdbcTemplate, @Qualifier("genreDbDao") GenreDao genreDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreDao = genreDao;
    }

    @Override
    public Film addFilm(Film film) {
        log.info("Запрос на добавление фильма: {} получен хранилищем БД", film.getName());

        //добавить информацию о фильме в таблицу films
        String addFilmSql = "INSERT INTO films(name,description,release_date,duration,rate,rating_id) VALUES(?,?,?,?,?,?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps =
                            connection.prepareStatement(addFilmSql, new String[]{"film_id"});
                    ps.setString(1, film.getName());
                    ps.setString(2, film.getDescription());
                    ps.setString(3, film.getReleaseDate().toString());
                    ps.setInt(4, film.getDuration());
                    ps.setInt(5, film.getRate());
                    ps.setInt(6, film.getMpa().getId());
                    return ps;
                },
                keyHolder);
        long filmId = keyHolder.getKey().intValue();
        film.setId(filmId);
        log.debug("Добавлен новый фильм с id={}", filmId);

        //если все жанры найдены в БД, то добавляем записи о жанрах в таблицу films_genre
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            log.debug("Добавляем жанры вновь создаваемому фильму с id={}.", filmId);
            Set<Integer> genres = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());
            for (int gr : genres) {
                genreDao.addFilmGenre(film.getId(), gr);
            }
            log.debug("Жанры для фильма с id={} добавлены.", filmId);
        }

        if (film.getDirectors() != null && !film.getDirectors().isEmpty())
            addDirectors(film.getDirectors(), film.getId());

        return getFilm(film.getId());
    }

    @Override
    //обновляем поля таблицы films: name, releaseDate, description, duration, rate, rating_id
    //поле rating_id сначала ищем в таблице ratings_mpa и если найден, то обновляем
    //обновляем поля таблицы films_genre:film_id, genre_id - удаляем и перезаписываем
    public Film updateFilm(Film film) {
        log.info("Получен запрос на обновление фильма с id={} в БД", film.getId());

        //обновляем данные в таблице films
        log.debug("Формируем sql запрос...");
        String updateFilmSql = "UPDATE films SET name=?,description=?,release_date=?,duration=?,rate=?," +
                "rating_id=? WHERE film_id=?;";
        Object[] args = new Object[]{film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getRate(), film.getMpa().getId(), film.getId()};

        int updateRow = jdbcTemplate.update(updateFilmSql, args);
        if (updateRow <= 0) {
            log.debug("Фильм с id={} для обновления не найден.", film.getId());
            throw new FilmNotFoundException("Фильм с id=" + film.getId() + " для обновления не найден.");
        }
        log.debug("Фильм с id={} обновлён.", film.getId());
        //если все жанры найдены в БД, то сначала удаляем записи из films_genre
        // потом добавляем записи о жанрах в таблицу films_genre
        genreDao.delFilmGenre(film.getId());
        log.debug("Обновляем жанры фильма с film_id={}, жанры: {}", film.getId(), film.getGenres());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Integer> genres = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());
            log.debug("id жанров для обновления: {}", genres.toString());
            for (int gr : genres) {
                genreDao.addFilmGenre(film.getId(), gr);
            }
            log.debug("Жанры фильма с film_id={} обновлены.", film.getId());
        }

        updateDirectors(film.getDirectors(), film.getId());

        return getFilm(film.getId());
    }

    @Override
    public void deleteFilm(long filmId) {
        log.debug("Получен запрос на удаление фильма с id={}", filmId);
        String deleteFilmSql = "delete from films where film_id= ?";
        Object[] args = new Object[]{filmId};
        int delRow = jdbcTemplate.update(deleteFilmSql, args);
        if (delRow <= 0) {
            log.debug("Фильм с id={} для удаления не найден.", filmId);
            throw new FilmNotFoundException("Фильм с id=" + filmId + " для удаления не найден.");
        }
        log.debug("Фильм с id={} удалён.", filmId);
    }

    @Override
    //возвращаемые поля:
    //из таблицы films: film_id, name, description, release_date, duration, rate,
    //genre - Set: genre.id...
    //из таблицы ratings_mpa: mpa.id,mpa.name
    public Film getFilm(long filmId) {
        log.debug("Получен запрос на фильм с id={};", filmId);
        String getFilmSql = "select f.FILM_ID\n" +
                "  ,f.NAME\n" +
                "  ,f.DESCRIPTION \n" +
                "  ,f.RELEASE_DATE \n" +
                "  ,f.DURATION \n" +
                "  ,f.RATE\n" +
                "  ,rm.RATING_ID\n" +
                "  ,rm.RATING_NAME\n" +
                "  ,GROUP_CONCAT(DISTINCT Concat(g.GENRE_ID,'-',g.GENRE_NAME) ORDER BY Concat(g.GENRE_ID,'-',g.GENRE_NAME)) AS GENRE_ID_NAME\n" +
                "  ,GROUP_CONCAT(DISTINCT Concat(d.DIRECTOR_ID, '-', d.NAME) ORDER BY Concat(d.DIRECTOR_ID, '-', d.NAME)) AS DIRECTOR_ID_NAME\n" +
                "from FILMS f \n" +
                "LEFT JOIN RATINGS_MPA rm  ON f.RATING_ID =rm.RATING_ID \n" +
                "LEFT JOIN FILMS_GENRE fg ON f.FILM_ID =fg.FILM_ID \n" +
                "LEFT JOIN GENRE g ON fg.GENRE_ID =g.GENRE_ID\n" +
                "LEFT JOIN FILMS_DIRECTOR fd ON f.FILM_ID = fd.FILM_ID\n" +
                "LEFT JOIN DIRECTORS d ON fd.DIRECTOR_ID = d.DIRECTOR_ID\n" +
                "WHERE f.FILM_ID = ? \n" +
                "GROUP BY f.FILM_ID;";
        List<Film> films = jdbcTemplate.query(getFilmSql, (rs, rowNum) -> filmMapper(rs), filmId);

        if (films.isEmpty()) {
            log.debug("Фильм не найден.");
            throw new FilmNotFoundException("Фильм не найден.");
        }

        return films.iterator().next();
    }

    @Override
    public List<Film> getFilms() {
        log.debug("Получен запрос на чтение всех фильмов");
        String getFilmSql = "select f.FILM_ID\n" +
                "  ,f.NAME\n" +
                "  ,f.DESCRIPTION \n" +
                "  ,f.RELEASE_DATE \n" +
                "  ,f.DURATION \n" +
                "  ,f.RATE\n" +
                "  ,rm.RATING_ID\n" +
                "  ,rm.RATING_NAME\n" +
                "  ,GROUP_CONCAT(DISTINCT Concat(g.GENRE_ID,'-',g.GENRE_NAME) ORDER BY Concat(g.GENRE_ID,'-',g.GENRE_NAME)) AS GENRE_ID_NAME\n" +
                "  ,GROUP_CONCAT(DISTINCT Concat(d.DIRECTOR_ID, '-', d.NAME) ORDER BY Concat(d.DIRECTOR_ID, '-', d.NAME)) AS DIRECTOR_ID_NAME\n" +
                "from films f LEFT JOIN RATINGS_MPA rm  ON f.RATING_ID =rm.RATING_ID \n" +
                "LEFT JOIN FILMS_GENRE fg ON f.FILM_ID =fg.FILM_ID \n" +
                "LEFT JOIN GENRE g ON fg.GENRE_ID =g.GENRE_ID\n" +
                "LEFT JOIN FILMS_DIRECTOR fd ON f.FILM_ID = fd.FILM_ID\n" +
                "LEFT JOIN DIRECTORS d ON fd.DIRECTOR_ID = d.DIRECTOR_ID\n" +
                "GROUP BY f.FILM_ID;";
        //запрашиваем все фильмы с жанрами и рейтингом MPA
        List<Film> films = jdbcTemplate.query(getFilmSql, (rs, rowNum) -> filmMapper(rs));
        log.debug("Получен список из {} фильмов.", films.size());
        return films;
    }

    @Override
    public List<Film> getPopularFilms(long maxCount) {
        String popFilmSql = "select f.FILM_ID\n" +
                "  ,f.NAME\n" +
                "  ,f.DESCRIPTION \n" +
                "  ,f.RELEASE_DATE \n" +
                "  ,f.DURATION \n" +
                "  ,f.RATE\n" +
                "  ,rm.RATING_ID\n" +
                "  ,rm.RATING_NAME\n" +
                "  ,GROUP_CONCAT(DISTINCT Concat(g.GENRE_ID,'-',g.GENRE_NAME) ORDER BY Concat(g.GENRE_ID,'-',g.GENRE_NAME)) AS GENRE_ID_NAME\n" +
                "  ,GROUP_CONCAT(DISTINCT Concat(d.DIRECTOR_ID, '-', d.NAME) ORDER BY Concat(d.DIRECTOR_ID, '-', d.NAME)) AS DIRECTOR_ID_NAME\n" +
                "from (\n" +
                "  SELECT fi.* \n" +
                "        FROM FILMS fi \n" +
                "        LEFT JOIN \n" +
                "        (SELECT FILM_ID,COUNT(*) cLike \n" +
                "            FROM FILMS_LIKE \n" +
                "            GROUP BY FILM_ID\n" +
                "        ) fil \n" +
                "        ON fil.FILM_ID = fi.FILM_ID \n" +
                "        ORDER BY clike DESC limit(?)\n" +
                ") f\n" +
                "LEFT JOIN RATINGS_MPA rm  ON f.RATING_ID =rm.RATING_ID \n" +
                "LEFT JOIN FILMS_GENRE fg ON f.FILM_ID =fg.FILM_ID \n" +
                "LEFT JOIN GENRE g ON fg.GENRE_ID =g.GENRE_ID\n" +
                "LEFT JOIN FILMS_DIRECTOR fd ON f.FILM_ID = fd.FILM_ID\n" +
                "LEFT JOIN DIRECTORS d ON fd.DIRECTOR_ID = d.DIRECTOR_ID\n" +
                "GROUP BY f.FILM_ID;";
        List<Film> popFilms = jdbcTemplate.query(popFilmSql, (rs, rowNum) -> filmMapper(rs), maxCount);

        log.debug("Популярные фильмы:");
        for (Film film : popFilms) {
            log.debug("Фильм с film_id={}: {}", film.getId(), film);
        }

        return popFilms;
    }

    @Override
    public List<Film> getDirectorsFilms(int directorId, String sortBy) {
        log.debug("Request to get directors films from DB.");

        String sql = "";

        if (sortBy.equals("likes"))
            sql = "select f.FILM_ID\n" +
                    "  ,f.NAME\n" +
                    "  ,f.DESCRIPTION \n" +
                    "  ,f.RELEASE_DATE \n" +
                    "  ,f.DURATION \n" +
                    "  ,f.RATE\n" +
                    "  ,rm.RATING_ID\n" +
                    "  ,rm.RATING_NAME\n" +
                    "  ,GROUP_CONCAT(DISTINCT Concat(g.GENRE_ID,'-',g.GENRE_NAME) ORDER BY Concat(g.GENRE_ID,'-',g.GENRE_NAME)) AS GENRE_ID_NAME\n" +
                    "  ,GROUP_CONCAT(DISTINCT Concat(d.DIRECTOR_ID, '-', d.NAME) ORDER BY Concat(d.DIRECTOR_ID, '-', d.NAME)) AS DIRECTOR_ID_NAME\n" +
                    "from (\n" +
                    "  SELECT fi.* \n" +
                    "        FROM FILMS fi \n" +
                    "        LEFT JOIN \n" +
                    "        (SELECT FILM_ID,COUNT(*) cLike \n" +
                    "            FROM FILMS_LIKE \n" +
                    "            GROUP BY FILM_ID\n" +
                    "        ) fil \n" +
                    "        ON fil.FILM_ID = fi.FILM_ID \n" +
                    "        ORDER BY clike  \n" +
                    ") f\n" +
                    "LEFT JOIN RATINGS_MPA rm  ON f.RATING_ID =rm.RATING_ID \n" +
                    "LEFT JOIN FILMS_GENRE fg ON f.FILM_ID =fg.FILM_ID \n" +
                    "LEFT JOIN GENRE g ON fg.GENRE_ID =g.GENRE_ID\n" +
                    "LEFT JOIN FILMS_DIRECTOR fd ON f.FILM_ID = fd.FILM_ID\n" +
                    "LEFT JOIN DIRECTORS d ON fd.DIRECTOR_ID = d.DIRECTOR_ID\n" +
                    "WHERE f.FILM_ID IN ( " +
                    "SELECT f2.film_id " +
                    "FROM films_director f2 " +
                    "WHERE f2.director_id = ?)\n" +
                    "GROUP BY f.FILM_ID;";
        else
            sql = "select f.FILM_ID\n" +
                    "  ,f.NAME\n" +
                    "  ,f.DESCRIPTION \n" +
                    "  ,f.RELEASE_DATE \n" +
                    "  ,f.DURATION \n" +
                    "  ,f.RATE\n" +
                    "  ,rm.RATING_ID\n" +
                    "  ,rm.RATING_NAME\n" +
                    "  ,GROUP_CONCAT(DISTINCT Concat(g.GENRE_ID,'-',g.GENRE_NAME) ORDER BY Concat(g.GENRE_ID,'-',g.GENRE_NAME)) AS GENRE_ID_NAME\n" +
                    "  ,GROUP_CONCAT(DISTINCT Concat(d.DIRECTOR_ID, '-', d.NAME) ORDER BY Concat(d.DIRECTOR_ID, '-', d.NAME)) AS DIRECTOR_ID_NAME\n" +
                    "from films f LEFT JOIN RATINGS_MPA rm  ON f.RATING_ID =rm.RATING_ID \n" +
                    "LEFT JOIN FILMS_GENRE fg ON f.FILM_ID =fg.FILM_ID \n" +
                    "LEFT JOIN GENRE g ON fg.GENRE_ID =g.GENRE_ID\n" +
                    "LEFT JOIN FILMS_DIRECTOR fd ON f.FILM_ID = fd.FILM_ID\n" +
                    "LEFT JOIN DIRECTORS d ON fd.DIRECTOR_ID = d.DIRECTOR_ID\n" +
                    "WHERE f.FILM_ID IN ( " +
                    "SELECT f2.film_id " +
                    "FROM films_director f2 " +
                    "WHERE f2.director_id = ? )\n" +
                    "GROUP BY f.FILM_ID\n" +
                    "ORDER BY f.RELEASE_DATE;";

        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> filmMapper(rs), directorId);
        log.debug("Получен список из {} фильмов.", films.size());
        return films;
    }

    private Film filmMapper(ResultSet rs) throws SQLException {
        //перебираем записи результирующего набора
        long id = rs.getLong("film_id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        LocalDate releaseDate = rs.getDate("release_date").toLocalDate();
        int duration = rs.getInt("duration");
        int rate = rs.getInt("rate");
        MPA mpa = new MPA();
        mpa.setId(rs.getInt("rating_id"));
        mpa.setName(rs.getString("rating_name"));

        Set<Genre> genres = getGenresFromResultSet(rs);

        Set<Director> directors = getDirectorsFromResultSet(rs);

        return Film.builder()
                .id(id)
                .name(name)
                .description(description)
                .releaseDate(releaseDate)
                .duration(duration)
                .rate(rate)
                .mpa(mpa)
                .genres(genres)
                .directors(directors)
                .build();
    }

    private Set<Director> getDirectorsFromResultSet(ResultSet rs) throws SQLException {
        Set<Director> directors = new HashSet<>();

        String allFilmDirectors = rs.getString("DIRECTOR_ID_NAME");

        if(allFilmDirectors == null || allFilmDirectors.isEmpty() || allFilmDirectors.isBlank() || allFilmDirectors.equals("-"))
            return directors;

        for (String filmDirector : allFilmDirectors.split(",")) {
            String[] strings = filmDirector.split("-");
            directors.add(
                    Director.builder()
                            .id(Integer.parseInt(strings[0]))
                            .name(strings[1])
                            .build()
            );
        }

        return directors;
    }

    private Set<Genre> getGenresFromResultSet(ResultSet rs) throws SQLException {
        Set<Genre> genres = new HashSet<>();

        String allFilmGenres = rs.getString("GENRE_ID_NAME");

        if (allFilmGenres == null || allFilmGenres.isEmpty() || allFilmGenres.isBlank() || allFilmGenres.equals("-"))
            return genres;

        for (String filmGenre : allFilmGenres.split(",")) {
            String[] strings = filmGenre.split("-");
            genres.add(
                    Genre.builder()
                    .id(Integer.parseInt(strings[0]))
                    .name(strings[1])
                    .build()
            );
        }

        return genres;
    }

    private void addDirectors(Set<Director> directors, long filmId) {
        log.debug("Request to add directors to DB.");

        String sql = "INSERT INTO films_director (film_id, director_id) " +
                "VALUES (?, ?);";

        for (Director director : directors) {
            jdbcTemplate.update(sql, filmId, director.getId());
        }
    }

    private void updateDirectors(Set<Director> directors, long filmId) {
        log.debug("Request to update directors to film with id = {}", filmId);

        String sql = "DELETE FROM films_director " +
                "WHERE film_id = ?;";
        jdbcTemplate.update(sql, filmId);

        if(directors != null)
            addDirectors(directors, filmId);
    }
}
