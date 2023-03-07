package ru.yandex.practicum.filmorate.storage.film.dao;

public interface FilmLikeDao {

    void addLike(long filmId, long userId);

    void deleteLike(long filmId, long userId);

}
