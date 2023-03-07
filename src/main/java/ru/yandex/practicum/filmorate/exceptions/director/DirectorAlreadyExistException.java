package ru.yandex.practicum.filmorate.exceptions.director;

public class DirectorAlreadyExistException extends RuntimeException {
	public DirectorAlreadyExistException(String message) {
		super(message);
	}
}
