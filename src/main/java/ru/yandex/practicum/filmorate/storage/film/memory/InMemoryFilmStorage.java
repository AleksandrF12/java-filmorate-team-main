package ru.yandex.practicum.filmorate.storage.film.memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.genre.GenreNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.dao.FilmDao;
import ru.yandex.practicum.filmorate.storage.film.dao.GenreDao;
import ru.yandex.practicum.filmorate.storage.film.dao.MpaDao;

import java.util.*;
import java.util.stream.Collectors;

//реализация методов добавления, удаления и модификации объектов.
@Component("filmInMemoryDao")
@Slf4j
public class InMemoryFilmStorage implements FilmDao {

    private int maxId = 0;

    private Map<Long, Film> films = new HashMap<>(); //информация о фильмах

    InMemoryFilmLikeDao inMemoryFilmLikeDao;
    GenreDao genreDao;
    MpaDao mpaDao;

    public InMemoryFilmStorage(@Qualifier("filmLikeInMemoryDao") InMemoryFilmLikeDao inMemoryFilmLikeDao,
                               @Qualifier("genreInMemoryDao") GenreDao genreDao,
                               @Qualifier("mpaInMemoryDao") MpaDao mpaDao) {
        this.inMemoryFilmLikeDao = inMemoryFilmLikeDao;
        this.genreDao = genreDao;
        this.mpaDao = mpaDao;
    }

    @Override
    public Film addFilm(Film film) {
        log.debug("Получен запрос на добавление фильма : {}", film);
        final long id = generateId();
        film.setId(id);
        if(film.getMpa()!=null) {
            film.setMpa(mpaDao.getRating(film.getMpa().getId()));
            log.debug("Фильму с id={} добавлены рейтинги MPAA: {}",film.getId(),film.getMpa());
        }
        if(film.getGenres()!=null) {
            TreeSet<Genre> genresNewFilm=new TreeSet<>((o1,o2)->o1.getId()-o2.getId());
            for(Genre gr:film.getGenres()) {
                if(genreDao.getGenge(gr.getId())!=null) {
                    genresNewFilm.add(genreDao.getGenge(gr.getId()));
                } else {
                    throw new GenreNotFoundException("Жанр с id="+gr.getId()+" не найден.");
                }
            }
            log.debug("Фильму с id={} добавлены жанры: {}",film.getId(),film.getGenres());
            film.setGenres(genresNewFilm);
        } else{
            film.setGenres(new HashSet<>());
        }
        this.films.put(id, film);
        log.info("Фильм добавлен : {}", this.films.get(id));
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        long filmId = film.getId();
        if (this.films.containsKey(filmId)) {
            //добавляем названия рейтингов MPAA
            if(film.getMpa()!=null) {
                film.setMpa(mpaDao.getRating(film.getMpa().getId()));
                log.debug("Фильму с id={} добавлены рейтинги MPAA: {}",film.getId(),film.getMpa());
            }

            //добавляем жанры
            if(film.getGenres()!=null) {
                TreeSet<Genre> genresNewFilm=new TreeSet<>((o1,o2)->o1.getId()-o2.getId());
                for(Genre gr:film.getGenres()) {
                    if(genreDao.getGenge(gr.getId())!=null) {
                        genresNewFilm.add(genreDao.getGenge(gr.getId()));
                    } else {
                        throw new GenreNotFoundException("Жанр с id="+gr.getId()+" не найден.");
                    }
                }
                log.debug("Фильму с id={} добавлены жанры: {}",film.getId(),film.getGenres());
                film.setGenres(genresNewFilm);
            }else{
                film.setGenres(new HashSet<>());
            }
            this.films.put(film.getId(), film);
            log.info("Фильм обновлён : {}", this.films.get(filmId));
            return film;
        }
        throw new FilmNotFoundException("Фильм с id=" + filmId + " не найден.");
    }

    //удаление фильма
    @Override
    public void deleteFilm(long filmId) {
        films.remove(filmId);
        log.info("Фильм с id={} удалён.", filmId);
    }

    //возвращает список всех фильмов
    @Override
    public List<Film> getFilms() {
        return new ArrayList<>(this.films.values());
    }

    //получение фильма по id
    @Override
    public Film getFilm(long filmId) {
        log.debug("Запрошен фильм с id={}", filmId);
        if(!this.films.containsKey(filmId)) {
            throw new FilmNotFoundException("Фильм с filmId="+filmId+" не найден.");
        }
        return this.films.get(filmId);
    }

    @Override
    public List<Film> getPopularFilms(long maxCount) {
        log.debug("Запрос на получение {} популярных фильмов...", maxCount);
        Map<Long, Integer> popFilms= inMemoryFilmLikeDao.getPopularFilms();
        for(Long pf:films.keySet()) {
            if(!popFilms.containsKey(pf)) {
                log.debug("Добавлен фильм filmId={} с нулевым рейтингом.",pf);
                popFilms.put(pf,0);
            }
        }
        log.debug("Найдено {} популярных фильмов.",popFilms.size());
        return popFilms.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .map(f->getFilm(f.getKey()))
                .limit(maxCount)
                .collect(Collectors.toList());
    }

    @Override
    public List<Film> getDirectorsFilms(int directorId, String sortBy) {
        log.debug("Request to get directors films from DB.");
        return null;
    }

    //генерация очередного id фильма
    private int generateId() {
        return ++maxId;
    }

}
