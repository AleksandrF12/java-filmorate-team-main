package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

@RestController
@RequestMapping("/genres")
@Slf4j
public class GenreController {
private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    //получение всех жанров
    @GetMapping
    protected List<Genre> getGenres() {
        log.info("Получен запрос на чтение жанров фильмов.");
        return genreService.getGenres();
    }

    //получение рейтинга MPA по id
    @GetMapping("/{id}")
    protected Genre getGenre(@PathVariable("id") int genreId) {
        log.info("Получен запрос на чтение жанра с id={}",genreId);
        return genreService.getGenre(genreId);
    }
}
