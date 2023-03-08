package ru.yandex.practicum.filmorate.storage.film.dao;

import java.util.List;

public interface FilmLikeDao {

    void addLike(long filmId, long userId);

    void deleteLike(long filmId, long userId);


}
