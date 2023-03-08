package ru.yandex.practicum.filmorate.storage.film.dao;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Set;

public interface FilmLikeDao {

    void addLike(long filmId, long userId);
    void deleteLike(long filmId, long userId);

    //получение фильмов для userId, которые лайкнули иные пользователи
    //и имеющие максимальное пересечение по лайкам с userId
    List<Film> getRecomendationFilm(long userId);
}
