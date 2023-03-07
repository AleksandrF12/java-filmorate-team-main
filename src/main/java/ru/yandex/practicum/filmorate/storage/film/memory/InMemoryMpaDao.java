package ru.yandex.practicum.filmorate.storage.film.memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.genre.GenreNotFoundException;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.film.dao.MpaDao;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Map.entry;

@Component("mpaInMemoryDao")
@Slf4j
public class InMemoryMpaDao implements MpaDao {
    private final LinkedHashMap<Integer, String> mpa = new LinkedHashMap<>(Map.ofEntries(
            entry(1, "G"),
            entry(2, "PG"),
            entry(3, "PG-13"),
            entry(4, "R"),
            entry(5, "NC-17")
    ));

    @Override
    public MPA getRating(int id) {
        if (mpa.containsKey(id)) {
            return new MPA(id, mpa.get(id));
        }
        throw new GenreNotFoundException("Рейтинг MPAA с id=" + id + " не найден.");
    }

    @Override
    public List<MPA> getRatings() {
        return mpa.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(k -> new MPA(k.getKey(), k.getValue()))
                .collect(Collectors.toList());
    }
}
