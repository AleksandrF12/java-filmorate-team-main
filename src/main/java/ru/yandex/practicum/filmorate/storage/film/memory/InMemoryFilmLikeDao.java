package ru.yandex.practicum.filmorate.storage.film.memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.storage.film.dao.FilmLikeDao;

import java.util.*;

//реализация методов добавления, удаления и модификации объектов.
@Component("filmLikeInMemoryDao")
@Slf4j
public class InMemoryFilmLikeDao implements FilmLikeDao {

    HashMap<Long, Set<Long>> likeUsers=new HashMap<>();
    @Override
    public void addLike(long filmId, long userId) {
        Optional<Set<Long>> likesFilm = Optional.ofNullable(this.likeUsers.get(filmId));
        Set<Long> newLikes=new HashSet<>();
        if (likesFilm.isPresent()) {
            newLikes = likesFilm.get();
            Optional<Long> likes = newLikes.stream().filter(u -> u == userId).findFirst();
            if (likes.isPresent()) {
                return;
            }
        }
        log.debug("Фильму с filmId={} добавлен лайк от пользователя userId={}", filmId, userId);
        newLikes.add(userId);
        likeUsers.put(filmId, newLikes);
    }

    @Override
    public void deleteLike(long filmId, long userId) {
        log.debug("Получен запрос на удаление лайка фильму с filmId={} пользователем с userId={}.", filmId, userId);
        Optional<Set<Long>> likesFilm = Optional.ofNullable(likeUsers.get(filmId));
        if (likesFilm.isPresent()) {
            Iterator<Long> userIter = likesFilm.get().iterator();
            while (userIter.hasNext()) {
                if (userIter.next() == userId) {
                    userIter.remove();
                    log.debug("Для фильма filmId={} удалён лайк пользователя с userId={}.", filmId, userId);
                }
            }
        }
    }

    public Map<Long, Integer> getPopularFilms() {
        Map<Long,Integer> popFilms=new HashMap<>();
        for(Long k:likeUsers.keySet()) {
            popFilms.put(k,likeUsers.get(k).size());
        }
        log.debug("Возвращено {} популярных фильмов.",popFilms.size());
        return popFilms;
    }

}
