package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.director.DirectorAlreadyExistException;
import ru.yandex.practicum.filmorate.exceptions.director.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.film.dao.DirectorDao;
import ru.yandex.practicum.filmorate.storage.film.daoImpl.DirectorDaoImpl;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class DirectorService {

	private final DirectorDao directorDao;

	@Autowired
	public DirectorService(DirectorDaoImpl directorDao) {
		this.directorDao = directorDao;
	}

	public List<Director> getAllDirectors() {
		return directorDao.getAllDirectors();
	}

	public Director getDirectorById(int directorId) {
		Optional<Director> director = directorDao.getDirectorById(directorId);

		if (director.isPresent())
			return director.get();
		else
			throw new DirectorNotFoundException("Director with id = " + directorId + " not found.");
	}

	public Director addDirector(Director director) {
		return directorDao.addDirector(director);
	}

	public Director updateDirector(Director director) {
		if(director.getId() == null)
			throw new DirectorNotFoundException("Director without id is not exist.");
		if(directorDao.getDirectorById(director.getId()).isEmpty() || director.getId() < 1)
			throw new DirectorNotFoundException("Director with id = " + director.getId()
					+ " is not exist.");
		return directorDao.updateDirector(director);
	}

	public void deleteDirector(int id) {
		directorDao.deleteDirector(id);
	}
}
