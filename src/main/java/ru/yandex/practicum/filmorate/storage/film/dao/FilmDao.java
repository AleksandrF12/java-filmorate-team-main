package ru.yandex.practicum.filmorate.storage.film.dao;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

//методы добавления, удаления и модификации объектов.

public interface FilmDao {

    //добавление фильма
    Film addFilm(Film film);

    //обновление данных о фильме
    Film updateFilm(Film film);

    //удаление фильма
    void deleteFilm(long filmId);

    Film getFilm(long filmId);
    List<Film> getFilms();
    List<Film> getPopularFilms(long maxCount);

    List<Film> getDirectorsFilms(int directorId, String sortBy);
}
