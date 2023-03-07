package ru.yandex.practicum.filmorate.exceptions.film;

public class FilmNotFoundException extends RuntimeException{
    public FilmNotFoundException(String message) {
        super(message);
    }
}
