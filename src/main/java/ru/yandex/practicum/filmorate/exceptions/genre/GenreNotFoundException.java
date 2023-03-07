package ru.yandex.practicum.filmorate.exceptions.genre;

public class GenreNotFoundException extends RuntimeException{
    public GenreNotFoundException(String message) {
        super(message);
    }
}
