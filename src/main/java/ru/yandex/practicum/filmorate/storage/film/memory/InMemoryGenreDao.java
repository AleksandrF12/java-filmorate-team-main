package ru.yandex.practicum.filmorate.storage.film.memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.mpa.MpaNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.dao.GenreDao;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Map.entry;

@Component("genreInMemoryDao")
@Slf4j
public class InMemoryGenreDao implements GenreDao {
    Map<Long, LinkedHashSet<Genre>> genresFilms = new HashMap<>();
    private final LinkedHashMap<Integer, String> genres = new LinkedHashMap<>(Map.ofEntries(
            entry(1, "Комедия"),
            entry(2, "Драма"),
            entry(3, "Мультфильм"),
            entry(4, "Триллер"),
            entry(5, "Документальный"),
            entry(6, "Боевик")
    ));

    @Override
    public Genre getGenge(int id) {
        if (this.genres.containsKey(id)) {
            return new Genre(id, this.genres.get(id));
        }
        throw new MpaNotFoundException("Жанр с id=" + id + " не найден.");
    }

    @Override
    public List<Genre> getGengesFilm(long filmId) {
        if(genresFilms.containsKey(filmId)) {
            return genresFilms.get(filmId).stream()
                    .sorted((g1,g2)->g1.getId()-g2.getId())
                    .collect(Collectors.toList());
        }
        throw new FilmNotFoundException("Фильм с filmId=" + filmId + " не найден.");
    }

    @Override
    public List<Genre> getGenresFilms() {
        return genres.entrySet().stream()
                .sorted((g1,g2)->g1.getKey()-g2.getKey())
                .map(k -> new Genre(k.getKey(), k.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public void addFilmGenre(long filmId, int genreId) {
        Optional<LinkedHashSet<Genre>> genresFilm = Optional.ofNullable(this.genresFilms.get(filmId));
        LinkedHashSet<Genre> newGenre = new LinkedHashSet<>();
        if (genresFilm.isPresent()) {
            newGenre = genresFilm.get();
            Optional<Genre> genre = newGenre.stream().filter(u -> u.getId() == genreId).findFirst();
            if (genre.isPresent()) {
                return;
            }
        }
        log.debug("Фильму с filmId={} добавлен очередной жанр с friendIв={}", filmId, genreId);
        newGenre.add(new Genre(genreId, genres.get(genreId)));
        genresFilms.put(filmId, newGenre);
    }

    @Override
    public void delFilmGenre(long filmId) {
        log.debug("Получен запрос на удаление жанров фильма с filmId={}.", filmId);
        if(genresFilms.containsKey(filmId)) {
            genresFilms.remove(filmId);
        }
    }
}
