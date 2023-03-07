package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.dao.GenreDao;

import java.util.List;

@Service
@Slf4j
public class GenreService {
    private final GenreDao genreDao;

    public GenreService(GenreDao genreDao) {
        this.genreDao = genreDao;
    }

    //возвращает информацию обо всех рейтингах MPA
    public List<Genre> getGenres() {
        return genreDao.getGenresFilms();
    }

    public Genre getGenre(int genreId) {
        return genreDao.getGenge(genreId);
    }
}
