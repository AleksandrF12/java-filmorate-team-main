package ru.yandex.practicum.filmorate.exceptions.user;

public class UserAlreadyExistException extends RuntimeException{
    public UserAlreadyExistException(String message) {
        super(message);
    }
}
