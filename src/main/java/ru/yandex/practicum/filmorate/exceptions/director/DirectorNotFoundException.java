package ru.yandex.practicum.filmorate.exceptions.director;

public class DirectorNotFoundException extends RuntimeException {

	public DirectorNotFoundException(String message) {
		super(message);
	}
}
