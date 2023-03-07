package ru.yandex.practicum.filmorate.storage.film.dao;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface GenreDao {
    Genre getGenge(int id);
    List<Genre> getGengesFilm(long filmId);

    List<Genre> getGenresFilms();

    void addFilmGenre(long filmId,int genreId);

    void delFilmGenre(long filmId);
}
