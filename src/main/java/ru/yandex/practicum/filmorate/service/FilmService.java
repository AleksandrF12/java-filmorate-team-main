package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.SortingIsNotSupportedException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.exceptions.director.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.user.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.film.dao.*;
import ru.yandex.practicum.filmorate.storage.film.daoImpl.DirectorDaoImpl;
import ru.yandex.practicum.filmorate.storage.user.dao.UserDao;


import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//отвечает за операции с фильмами, — добавление и удаление лайка, вывод 10 наиболее популярных фильмов
// по количеству лайков. Пусть пока каждый пользователь может поставить лайк фильму только один раз.

@Service
@Slf4j
public class FilmService {
    private final FilmDao filmStorage;
    private final UserDao userStorage;
    private final MpaDao mpaDao;
    private final FilmLikeDao filmLikeDao;
    private final GenreDao genreDao;
    private final DirectorDao directorDao;

    public FilmService(FilmDao filmStorage,
                       UserDao userStorage,
                       MpaDao mpaDao,
                       FilmLikeDao filmLikeDao,
                       GenreDao genreDao,
                       DirectorDao directorDao) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaDao = mpaDao;
        this.filmLikeDao = filmLikeDao;
        this.genreDao = genreDao;
        this.directorDao = directorDao;
    }

    //добавляем фильм
    public Film addFilm(Film film) {
        log.info("Запрос на добавление фильма: {} направлен в хранилище...",film.getName());

        //проверка наличия рейтинга в таблице ratings (MPA)
        if (!isRatingsMpa(film.getMpa().getId())) {
            throw new ValidationException("Не найден рейтинг фильма с id=" + film.getMpa().getId());
        }

        //проверка наличия жанра в таблице genres
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            if (!isGenres(film.getGenres())) {
                throw new ValidationException("Для обновляемого фильма не найдены все жанры.");
            }
        }

        //проверка наличия режиссера в таблице directors
        if (film.getDirectors() != null && !film.getDirectors().isEmpty())
            if (!isDirectors(film.getDirectors()))
                throw new ValidationException("Для фильма не найдены все режиссеры.");

        return filmStorage.addFilm(film);
    }

    //обновляем фильм
    public Film updateFilm(Film film) {
        isValidFilmId(film.getId());
        //проверка наличия рейтинга в таблице ratings (MPA)
        //если он задан
        if (!isRatingsMpa(film.getMpa().getId())) {
            throw new ValidationException("Не найден рейтинг фильма с id=" + film.getMpa().getId());
        }

        //поиск жанра в таблице genres
        //если получено пустое поле с жанрами, то игнорируем проверку
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            if (!isGenres(film.getGenres())) {
                throw new ValidationException("Для обновляемого фильма не найдены все жанры.");
            }
        }

        if ((film.getDirectors() != null && !film.getDirectors().isEmpty()) && !isDirectors(film.getDirectors()))
            throw new ValidationException("Для фильма не найдены все режиссеры.");

        return filmStorage.updateFilm(film);
    }

    //удаление фильма по id
    public void deleteFilm(long filmId) {
        isValidFilmId(filmId);
        filmStorage.deleteFilm(filmId);
    }

    //получение фильма по id
    public Film getFilm(long filmId) {
        log.info("GET Запрос на поиск фильма с id={}", filmId);
        isValidFilmId(filmId);
        return filmStorage.getFilm(filmId);
    }

    //возвращает информацию обо всех фильмах
    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    //пользователь ставит лайк фильму.
    public void addLike(long filmId, long userId) {
        log.debug("Запрос на добавление фильму с id={} лайка от пользователя с userId={}", filmId, userId);
        //проверка существования фильма с id
        isValidFilmId(filmId);
        isValidUserId(userId);
        Film film=filmStorage.getFilm(filmId);
        if(film==null) {
            throw new FilmNotFoundException("Фильм с id="+filmId+" не найден.");
        }
        //проверка существования пользователя с id
        User user=userStorage.getUser(userId);
        if(user==null) {
            throw new UserNotFoundException("Пользователь с id=" + userId + " не найден.");
        }
        filmLikeDao.addLike(filmId,userId);
    }

    //пользователь удаляет лайк.
    public void deleteLike(long filmId, long userId) {
        log.debug("Запрос на удаление лайка фильму с id={} лайка от пользователя с userId={}", filmId, userId);
        isValidFilmId(filmId);
        isValidUserId(userId);
        filmLikeDao.deleteLike(filmId,userId);
    }

    //вывод популярных фильмов,если параметр не задан, то выводим 10 фильмов
    public List<Film> getPopularFilms(long count) {
        //проверка корректности значения count : null, меньше 0
        if (count <= 0) {
            throw new ValidationException("Запрошено отрицательное количество популярных фильмов.");
        }
        log.debug("Запрос на получение {} популярных фильмов...", count);
        return filmStorage.getPopularFilms(count);
    }

    public List<Film> getDirectorFilms(int directorId, String sortBy) {
        if (directorDao.getDirectorById(directorId).isEmpty()) {
            log.debug("Director with id = {} is not exist.", directorId);
            throw new DirectorNotFoundException("Director with id = {" + directorId  + "} is not exist.");
        }

        if (!sortBy.equals("year") && !sortBy.equals("likes")) {
            log.debug("Sorting {} is not supported.", sortBy);
            throw new SortingIsNotSupportedException("Sorting " + sortBy + " is not supported.");
        }

        return filmStorage.getDirectorsFilms(directorId, sortBy);
    }

    //вернуть общие фильмы для пользователей
    public List<Film> getCommonFilms(Optional<String> userId, Optional<String> friendId) {
        log.info("FilmService: Запрошены общие фильмы пользователей.");
        long userIdTrue = getDigitOfString(userId);
        long friendIdTrue = getDigitOfString(friendId);
        //проверка значений userId и friendId как на значение >0, так и на соответствие Long
        log.info("FilmService: Запрос на получение общих фильмов пользователей с userId={} и friendId={}..."
                , userIdTrue, friendIdTrue);
        isValidUserId(userIdTrue);
        isValidUserId(friendIdTrue);
        isNotEqualIdUser(userIdTrue, friendIdTrue);
        return filmStorage.getCommonFilms(userIdTrue, friendIdTrue);
    }

    //проверка корректности значений filmId
    private boolean isValidFilmId(long filmId) {
        if (filmId <= 0) {
            throw new FilmNotFoundException("Некорректный id фильма.");
        }
        return true;
    }

    //проверка корректности значений filmId
    private boolean isValidUserId(long userId) {
        if (userId <= 0) {
            throw new UserNotFoundException("Некорректный id пользователя.");
        }
        return true;
    }

    //проверка наличие видов рейтингов добавляемого/обновляемого фильма в БД
    private boolean isRatingsMpa(int mpaId) {
        MPA ratingMpa = mpaDao.getRating(mpaId);
        if (ratingMpa == null) {
            log.debug("Не найден рейтинг фильма с id={}", mpaId);
            return false;
        }
        return true;
    }

    //проверка наличие видов жанров добавляемого/обновляемого фильма в БД
    private boolean isGenres(Set<Genre> genres) {
        Set<Integer> genresId = genreDao.getGenresFilms().stream().map(g -> g.getId()).collect(Collectors.toSet());
        for (Genre gr : genres) {
            if (!genresId.contains(gr.getId())) {
                log.debug("Для фильма не найден жанр с id=" + gr.getId());
                return false;
            }
        }
        log.debug("Для фильма не найден все добавляемые (обновляемые) жанры.");
        return true;
    }

    private boolean isDirectors(Set<Director> directors) {
        List<Integer> directorsId = directorDao.getAllDirectors().stream().map(Director::getId).collect(Collectors.toList());
        for(Director director : directors) {
            if(!directorsId.contains(director.getId())) {
                log.debug("Для фильма не найден директор с id=" + director.getId());
                return false;
            }
        }
        return true;
    }

    //возвращает из строки числовое значение

    private Long getDigitOfString(Optional<String> str) {
        return Stream.of(str.get())
                .limit(1)
                .map(this::stringParseLong)
                .findFirst()
                .get();
    }


    //проверяет не равныли id пользователя и друга
    public boolean isNotEqualIdUser(long userId, long friendId) {
        if (userId == friendId) {
            throw new UserNotFoundException("Пользователь с id=" + userId + " не может добавить сам себя в друзья.");
        }
        return true;
    }

    private Long stringParseLong(String str) {
        try {
            return Long.parseLong(str);
        } catch (RuntimeException e) {
            throw new ValidationException("Передан некорректный userId.");
        }
    }

}
