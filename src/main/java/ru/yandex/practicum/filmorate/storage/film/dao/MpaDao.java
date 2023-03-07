package ru.yandex.practicum.filmorate.storage.film.dao;

import ru.yandex.practicum.filmorate.model.MPA;

import java.util.List;

public interface MpaDao {
    MPA getRating(int id);
    List<MPA> getRatings();
}
