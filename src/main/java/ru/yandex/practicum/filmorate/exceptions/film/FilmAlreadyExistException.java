package ru.yandex.practicum.filmorate.exceptions.film;

public class FilmAlreadyExistException extends RuntimeException{
    public FilmAlreadyExistException(String message) {
        super(message);
    }
}
