package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.film.dao.MpaDao;

import java.util.List;

@Service
@Slf4j
public class MpaService {
    private final MpaDao mpaDao;

    public MpaService(MpaDao mpaDao) {
        this.mpaDao = mpaDao;
    }

    //возвращает информацию обо всех рейтингах MPA
    public List<MPA> getMpas() {
        return mpaDao.getRatings();
    }

    public MPA getMpa(int mpaId) {
        return mpaDao.getRating(mpaId);
    }
}
