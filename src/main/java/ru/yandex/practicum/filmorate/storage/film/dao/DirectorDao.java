package ru.yandex.practicum.filmorate.storage.film.dao;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface DirectorDao {

	Optional<Director> getDirectorById(int directorId);

	List<Director> getAllDirectors();

	Director addDirector(Director director);

	Director updateDirector(Director director);

	void deleteDirector(int directorId);
}
