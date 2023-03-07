package ru.yandex.practicum.filmorate.storage.film.daoImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.director.DirectorAlreadyExistException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.dao.DirectorDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class DirectorDaoImpl implements DirectorDao {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public DirectorDaoImpl (JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public Optional<Director> getDirectorById(int directorId) {
		log.debug("Request to get director by id = {} from DB.", directorId);

		String sql = "SELECT * " +
				"FROM directors " +
				"WHERE director_id = ?;";
		List<Director> directors = jdbcTemplate.query(sql, (rs, rowNum) -> makeDirector(rs), directorId);
		if (directors.isEmpty())
			return Optional.empty();
		else
			return Optional.of(directors.iterator().next());
	}

	@Override
	public List<Director> getAllDirectors() {
		log.debug("Request to get all directors from DB.");

		String sql = "SELECT * " +
				"FROM directors;";

		return jdbcTemplate.query(sql, (rs, rowNum) -> makeDirector(rs));
	}

	@Override
	public Director addDirector(Director director) {
		log.debug("Request to add director to DB {}.", director);

		if (contains(director))
			throw new DirectorAlreadyExistException("Director with name = " + director.getName()
					+ " is already exist.");

		String sql = "INSERT INTO directors (name) " +
				"VALUES (?);";

		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(
				connection -> {
					PreparedStatement ps =
							connection.prepareStatement(sql, new String[]{"director_id"});
					ps.setString(1, director.getName());
					return ps;
				},
				keyHolder);
		int directorId = keyHolder.getKey().intValue();
		director.setId(directorId);

		return director;
	}

	@Override
	public Director updateDirector(Director director) {
		log.debug("Request to update director into DB.");

		String sql = "UPDATE directors " +
				"SET name = ? " +
				"WHERE director_id = ?;";
		jdbcTemplate.update(sql, director.getName(), director.getId());

		return director;
	}

	@Override
	public void deleteDirector(int directorId) {
		log.debug("Request to delete director by id = {} from DB.", directorId);

		String sql = "DELETE FROM directors " +
				"WHERE director_id = ?;";
		jdbcTemplate.update(sql, directorId);
	}

	private boolean contains(Director director) {
		log.debug("Checking that the director with name = {} is in DB.", director.getName());

		String sql = "SELECT * " +
				"FROM directors " +
				"WHERE name = ?;";
		SqlRowSet directorRow = jdbcTemplate.queryForRowSet(sql, director.getName());
		return directorRow.next();
	}

	private Director makeDirector(ResultSet rs) throws SQLException {
		log.debug("Request to makeDirector from DB.");

		return Director.builder()
				.id(rs.getInt("director_id"))
				.name(rs.getString("name"))
				.build();
	}
}
