package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exceptions.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.genre.GenreNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.mpa.MpaNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.user.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.dao.FilmLikeDao;
import ru.yandex.practicum.filmorate.storage.film.daoImpl.FilmDbDao;
import ru.yandex.practicum.filmorate.storage.film.daoImpl.GenreDbDao;
import ru.yandex.practicum.filmorate.storage.film.daoImpl.MpaDbDao;
import ru.yandex.practicum.filmorate.storage.user.dao.FriendsDao;
import ru.yandex.practicum.filmorate.storage.user.daoImpl.UserDbDao;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmoRateApplicationTests {

    private final UserDbDao userStorage;
    private final FriendsDao friendsDao;
    private final FilmDbDao filmDbStorage;
    private final FilmLikeDao filmLikeDao;
    private final MpaDbDao mpaDbStorage;
    private final GenreDbDao genreDbStorage;

    private final User user1 = new User(1, "user1_test_1@email.ru", "user1_login", "user1_name",
            LocalDate.of(2002, 5, 3));
    private final User user1Update = new User(1, "user1_test_1_Update@email.ru", "user1_login_update", "",
            LocalDate.of(2002, 4, 3));
    private final User userUnknown = new User(9999, "user1_test_1@email.ru", "user1_login", "user1_name",
            LocalDate.of(2002, 5, 3));
    private final Film film = new Film(1, "film1_test_name", "film1_test_description",
            LocalDate.of(2018, 1, 1), 180, 4, new MPA(1, "R"),
            Set.of(new Genre(1, "Комедия"), new Genre(4, "Триллер")));
    private final Film filmUpdateUnknown = new Film(9999, "film1_test_name", "film1_test_description",
            LocalDate.of(2018, 1, 1), 180, 4, new MPA(1, "R"),
            Set.of(new Genre(1, "Комедия"), new Genre(4, "Триллер")));
    private final Film filmUpdateCorrect = new Film(5, "film1_test_name_update",
            "film1_test_description_update", LocalDate.of(2019, 1, 1), 170, 7,
            new MPA(1, "G"), Set.of(new Genre(5, "Документальный"), new Genre(4, "Триллер")));

    @Test
    public void testCrudUsers() {
        //создание пользователя: id пользователя, имя пользователя и др. данные
        User user = userStorage.addUser(user1);
        User userGet = userStorage.getUser(user.getId());
        assertEquals(7, userGet.getId(), "id добавленного пользователя не совпадает.");
        assertEquals("user1_name", userGet.getName(), "name добавленного пользователя не совпадает.");
        // обновление пользователя с неизвестным id: код возвращаемой ошибки
        assertThrows(UserNotFoundException.class, () -> userStorage.updateUser(userUnknown));
        // обновление пользователя с корректным id: id пользователя, имя пользователя и др. данные
        user = userStorage.updateUser(user1Update);
        assertEquals("user1_login_update", user.getName(), "name добавленного пользователя не совпадает.");
        assertThat(user).hasFieldOrPropertyWithValue("name", "user1_login_update");
    }

    @Test
    public void testGetUser() {
        User user = userStorage.getUser(2);
        assertThat(user).hasFieldOrPropertyWithValue("name", "user2_name");
    }

    @Test
    public void testCrudUserFriends() {
        //узнать нескольких общих друзей пользователей
        List<User> friendsUser1 = friendsDao.getFriends(1);
        List<User> friendsUser4 = friendsDao.getFriends(4);
        List<User> commonFriends = friendsDao.getCommonFriends(1, 4);
        assertEquals(2, commonFriends.size(), "Количество общих друзей пользователей не совпадает.");
        assertEquals(2, commonFriends.get(0).getId(), "Друзья пользователей с id =1,4 не совпадают.");
        assertEquals(3, commonFriends.get(1).getId(), "Друзья пользователей с id =1,4 не совпадают.");

        //добавить друга: обоюдная и необоюдная дружба
        friendsDao.addFriend(1, 5);
        friendsUser1 = friendsDao.getFriends(1);
        List<User> friendsUser5 = friendsDao.getFriends(5);
        assertEquals(2, friendsUser1.get(0).getId(), "Друзья пользователя с id=1 не совпадают.");
        assertEquals(3, friendsUser1.get(1).getId(), "Друзья пользователя с id=1 не совпадают.");
        assertEquals(5, friendsUser1.get(2).getId(), "Друзья пользователя с id=1 не совпадают.");
        assertEquals(0, friendsUser5.size(), "У пользователя с id=5 не должно быть друзей.");
        friendsDao.addFriend(5, 1);
        friendsUser5 = friendsDao.getFriends(5);
        assertEquals(1, friendsUser5.get(0).getId(), "Друзья пользователя с id=5 не совпадают.");
        //удалить друга
        friendsDao.deleteFriend(5, 1);
        friendsUser5 = friendsDao.getFriends(5);
        assertEquals(0, friendsUser5.size(), "У пользователя с id=5 не должно быть друзей.");
    }

    @Test
    public void testCrudFilms() {
        //добавление фильма
        filmDbStorage.addFilm(film);
        Film filmTest1 = filmDbStorage.getFilm(5);
        assertEquals("film1_test_name", filmTest1.getName(), "name добавленного фильма не совпадает.");
        //получаем фильм с неизвестным id
        assertThrows(FilmNotFoundException.class, () -> filmDbStorage.getFilm(9999));
        //обновление фильма с несуществующим id
        assertThrows(FilmNotFoundException.class, () -> filmDbStorage.updateFilm(filmUpdateUnknown));
        //обновление фильма с корректным id
        Film filmTestUpdate = filmDbStorage.updateFilm(filmUpdateCorrect);
        assertEquals("film1_test_name_update", filmTestUpdate.getName());
        //удаление фильма с несуществующим id
        assertThrows(FilmNotFoundException.class, () -> filmDbStorage.deleteFilm(9999));
        //удаление фильма с корректным id
        filmDbStorage.deleteFilm(5);
        assertThrows(FilmNotFoundException.class, () -> filmDbStorage.getFilm(5));
        //получение списка фильмов
        List<Film> films = filmDbStorage.getFilms();
        assertEquals(4, films.size(), "Количество фильмов не совпадает.");
        assertEquals("Interstellar", films.get(0).getName(), "name фильма с id=1 не совпадает.");
        assertEquals("The Green Mile", films.get(1).getName(), "name фильма с id=2 не совпадает.");
        assertEquals("Back to the Future", films.get(2).getName(), "name фильма с id=3 не совпадает.");
        assertEquals("The Lion King", films.get(3).getName(), "name фильма с id=4 не совпадает.");
    }

    @Test
    public void testCrudFilmLike() {
        //самые популярные фильмы
        List<Film> popularFilms = filmDbStorage.getPopularFilms(5);
        assertEquals(4, popularFilms.size(), "Количество популярных фильмов не совпадает.");
        assertEquals(2, popularFilms.get(0).getId(), "Самый популярный фильм не совпадает.");
        assertEquals(4, popularFilms.get(3).getId(), "Самый НЕ популярный фильм не совпадает.");
        //добавляем лайк фильму с несуществующим id
        assertThrows(FilmNotFoundException.class, () -> filmLikeDao.addLike(9999, 1));
        //добавляем лайк фильму с корректным id
        filmLikeDao.addLike(1, 1);
        filmLikeDao.addLike(1, 3);
        popularFilms = filmDbStorage.getPopularFilms(1);
        assertEquals(1, popularFilms.get(0).getId(), "Самый популярный фильм не совпадает.");
        //удаляем лайк фильму с несуществующим id
        assertThrows(FilmNotFoundException.class, () -> filmLikeDao.deleteLike(9999, 1));
        //удаляем лайк фильму с корректным id
        filmLikeDao.deleteLike(1, 1);
        filmLikeDao.deleteLike(1, 3);
        popularFilms = filmDbStorage.getPopularFilms(1);
        assertEquals(2, popularFilms.get(0).getId(), "Самый популярный фильм не совпадает.");
    }

    @Test
    public void testMpa() {
        //получаем список всех рейтингов MPA
        List<MPA> ratings = mpaDbStorage.getRatings();
        assertEquals(5, ratings.size(), "Количество известный рейтингов MPA не совпадает.");
        //получаем рейтинг MPA по некорректному id
        assertThrows(MpaNotFoundException.class, () -> mpaDbStorage.getRating(9999));
        //получаем рейтинг MPA по корректному id
        MPA rating = mpaDbStorage.getRating(1);
        assertNotNull(rating, "Ретинг MPA должен существовать.");
        assertEquals("G", rating.getName(), "Название рейтинга с id=1 не совпадает.");
    }

    @Test
    public void testGenres() {
        //добавить фильму жанр c некорректным id жанра
        assertThrows(GenreNotFoundException.class, () -> genreDbStorage.addFilmGenre(1,9999));
        //добавить фильму жанр c некорректным id фильма
        assertThrows(GenreNotFoundException.class, () -> genreDbStorage.addFilmGenre(9999,4));
        //добавить фильму жанр с корректными id addFilmGenre
        genreDbStorage.addFilmGenre(1,4);
        List<Genre> genres=genreDbStorage.getGengesFilm(1);
        assertNotNull(genres,"Жанры для фильма с id=1 отсутствуют.");
        assertEquals(2,genres.size(),"Количество жанров для фильма с id=1 не совпадает.");

        //удалить у фильма жанры с корректным id фильма
        genreDbStorage.delFilmGenre(1);
        genres=genreDbStorage.getGengesFilm(1);
        assertEquals(0,genres.size(),"Жанры для фильма с id=1 должны отсутствовать.");


        //получить жанр по НЕ корректному id
        assertThrows(GenreNotFoundException.class, () -> genreDbStorage.getGenge(9999));
        //получить жанр по корректному id
        Genre genre1 = genreDbStorage.getGenge(1);
        Genre genre5 = genreDbStorage.getGenge(5);
        assertNotNull(genre1, "Жанр с id=1 должен существовать.");
        assertNotNull(genre5, "Жанр с id=5 должен существовать.");
        assertEquals("Комедия", genre1.getName(), "Название жанра с id=1 не совпадает.");
        assertEquals("Документальный", genre5.getName(), "Название жанра с id=1 не совпадает.");
        //получить жанры по filmId
        List<Genre> genresFilm1 = genreDbStorage.getGengesFilm(2);
        assertEquals("Драма", genresFilm1.get(0).getName(), "Жанры фильм с id=2 не совпадают.");
        assertEquals("Триллер", genresFilm1.get(1).getName(), "Жанры фильм с id=2 не совпадают.");
        //получить все жанры
        genres = genreDbStorage.getGenresFilms();
        assertNotNull(genres, "Список фильмов пустой.");
        assertEquals(6, genres.size(), "Количество фильмов не совпадает.");
        assertEquals("Комедия", genres.get(0).getName(), "Название жанра с id=1 не совпадает.");
        assertEquals("Боевик", genres.get(5).getName(), "Название жанра с id=5 не совпадает.");
    }

}

