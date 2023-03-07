package ru.yandex.practicum.filmorate.storage.film.daoImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.mpa.MpaNotFoundException;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.film.dao.MpaDao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component("mpaDbDao")
@Primary
@Slf4j
public class MpaDbDao implements MpaDao {

    private final JdbcTemplate jdbcTemplate;

    public MpaDbDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public MPA getRating(int id) {
        log.debug("Получен запрос на поиск рейтинга MPA с id={}", id);
        String getMpaSql = "select rating_id,rating_name from ratings_mpa where rating_id = ?";
        MPA mpa = jdbcTemplate.query(getMpaSql, (rs, rowNum) -> mpaMapper(rs), id).stream().findAny().orElse(null);

        if (mpa == null) {
            log.debug("Рейтинг с id={} не найден.", id);
            throw new MpaNotFoundException("Рейтинг MPA с id=" + id + " не найден.");
        }
        log.debug("Рейтинг с id={} найден.", id);
        return mpa;
    }

    @Override
    public List<MPA> getRatings() {
        String getMpaSql = "select rating_id,rating_name from ratings_mpa order by rating_id;";
        List<MPA> mpas = jdbcTemplate.query(getMpaSql, (rs, rowNum) -> mpaMapper(rs));
        if (mpas == null) {
            throw new MpaNotFoundException("Рейтинг MPA не найдены");
        }
        return mpas;
    }

    private MPA mpaMapper(ResultSet rs) throws SQLException {
        //перебираем записи результирующего набора
        int id = rs.getInt("rating_id");
        String name = rs.getString("rating_name");
        return new MPA(id, name);
    }
}
