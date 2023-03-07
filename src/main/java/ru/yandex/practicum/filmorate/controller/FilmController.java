package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    //добавление фильма
    @PostMapping
    protected Film addFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос на добавление фильма: {}",film.getName());
        return filmService.addFilm(film);
    }

    //обновление фильма
    @PutMapping
    protected Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос на обновление фильма: {}",film.getId());
        return filmService.updateFilm(film);
    }

    //удаление фильма по id
    @DeleteMapping("/{id}")
    protected void deleteFilm(@PathVariable("id") long filmId) {
        log.info("Получен запрос на удаление фильма: {}",filmId);
        filmService.deleteFilm(filmId);
    }

    //получение фильма по id
    @GetMapping("/{id}")
    protected Film getFilm(@PathVariable("id") long filmId) {
        log.info("Получен запрос на чтение фильма с id={}",filmId);
        return filmService.getFilm(filmId);
    }

    //возвращает информацию обо всех фильмах
    @GetMapping
    protected List<Film> getFilms() {
        log.info("Получен запрос на чтение всех фильмов.");
        return filmService.getFilms();
    }

    //пользователь ставит лайк фильму
    @PutMapping("/{id}/like/{userId}")
    protected void addLike(@PathVariable("id") long filmId, @PathVariable("userId") long userId) {
        filmService.addLike(filmId, userId);
    }

    //пользователь удаляет лайк у фильма
    @DeleteMapping("/{id}/like/{userId}")
    protected void deleteLike(@PathVariable("id") long filmId, @PathVariable("userId") long userId) {
        filmService.deleteLike(filmId, userId);
    }

    //вернуть самые популярные фильмы
    @GetMapping("/popular")
    protected List<Film> getPopularFilms(@RequestParam(defaultValue = "10", required = false) Long count) {
        log.info("1.Запрос на получение {} популярных фильмов...", count);
        return filmService.getPopularFilms(count);
    }

    @GetMapping("/director/{directorId}")
    protected List<Film> getDirectorFilms(@PathVariable int directorId,
                                          @RequestParam String sortBy) {
        log.debug("Request to get directors films. SortBy = " + sortBy + ".");
        return filmService.getDirectorFilms(directorId, sortBy);
    }
}
