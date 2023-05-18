package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Genre> findAll() {
        String sql = "select * from genres";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeGenre(rs));
    }

    @Override
    public Genre findGenreById(int id) {
        SqlRowSet genreRows = jdbcTemplate.queryForRowSet("select * from genres where genre_id = ?", id);

        if(genreRows.next()) {
            return new Genre(genreRows.getInt("genre_id"), genreRows.getString("name"));
        } else {
            throw new GenreNotFoundException(String.format("Жанр № %d не найден", id));
        }
    }

    @Override
    public List<Genre> findFilmGenres(long filmId) {
        String sql = "select * from genres where genre_id in " +
                "(select genre_id from film_genres where film_id = ?)";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeGenre(rs), filmId);
    }

    @Override
    public void addFilmGenres(long filmId, Collection<Genre> genres) {
        String sql = "insert into film_genres (film_id, genre_id) values (?, ?)";

        for (Genre genre: genres) {
            int genreId = genre.getId();
            jdbcTemplate.update(sql, filmId, genreId);
        }
    }

    @Override
    public void updateFilmGenres(long filmId, Collection<Genre> genres) {
        String sql = "delete from film_genres where film_id = ?";
        jdbcTemplate.update(sql, filmId);

        addFilmGenres(filmId, genres);
    }

    private Genre makeGenre(ResultSet rs) throws SQLException {
        return new Genre(rs.getInt("genre_id"), rs.getString("name"));
    }
}
