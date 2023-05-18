package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component("filmDbStorage")
@Slf4j
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    @Override
    public Film create(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("film_id");

        long filmId = simpleJdbcInsert.executeAndReturnKey(film.toMap()).longValue();
        film.setId(filmId);


        updateMpaAndGenres(film);
        genreStorage.addFilmGenres(filmId, film.getGenres());

        log.debug("Добавлен фильм: {}", film);
        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "update films set name = ?, description = ?, release_date = ?, " +
                "duration = ?, mpa_id =? where film_id = ?";
        int responseNumber = jdbcTemplate.update(
                sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        if(responseNumber != 0) {
            updateMpaAndGenres(film);
            genreStorage.updateFilmGenres(film.getId(), film.getGenres());

            log.debug("Обновлён фильм: {}", film);
            return film;
        } else {
            log.warn("Фильм с ID {} не найден", film.getId());
            throw new FilmNotFoundException(String.format("Фильм № %d не найден", film.getId()));
        }
    }

    @Override
    public List<Film> findAll() {
        String sql = "select * from films";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
    }

    @Override
    public Film findFilmById(long id) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("select * from films where film_id = ?", id);

        if(filmRows.next()) {
            Film film = new Film(
                    filmRows.getLong("film_id"),
                    filmRows.getString("name"),
                    filmRows.getString("description"),
                    Objects.requireNonNull(filmRows.getDate("release_date")).toLocalDate(),
                    filmRows.getLong("duration"),
                    mpaStorage.findMpaById(filmRows.getInt("mpa_id"))
            );
            uploadFilmGenres(film);
            uploadLikes(film);

            log.debug("Получен фильм с ID {}", film.getId());

            return film;
        } else {
            log.warn("Фильм с ID {} не найден", id);
            throw new FilmNotFoundException(String.format("Фильм № %d не найден", id));
        }
    }

    @Override
    public List<Film> findPopular(int count) {
        String sql = "select * from films where film_id in " +
                "(select film_id from likes group by film_id " +
                "order by count(user_id) desc limit ?)";
        List<Film> popular = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), count);

        if (popular.size() < count) {
            int limit = count - popular.size();
            sql = "select * from films limit ?";
            List<Film> additional = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), limit);
            popular.addAll(additional);
        }
        return popular.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public void like(long filmId, long userId) {
        String sql = "insert into likes (film_id, user_id) values (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
        log.debug("Пользователь № {} поставил лайк фильму № {}", userId, filmId);
    }

    @Override
    public void unlike(long filmId, long userId) {
        String sql = "delete from likes where film_id = ? and user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
        log.debug("Пользователь № {} убрал лайк с фильма № {}", userId, filmId);
    }

    private Film makeFilm(ResultSet rs) throws SQLException {
        Film film = new Film(
                rs.getLong("film_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("release_date").toLocalDate(),
                rs.getLong("duration"),
                mpaStorage.findMpaById(rs.getInt("mpa_id"))
        );
        uploadFilmGenres(film);
        uploadLikes(film);
        return film;
    }

    private void updateMpaAndGenres(Film film) {
        if (film.getMpa() == null || film.getGenres().isEmpty()) {
            return;
        }

        Mpa mpa = mpaStorage.findMpaById(film.getMpa().getId());
        film.setMpa(mpa);

        Set<Genre> genres = film.getGenres().stream()
                .sorted(Comparator.comparing(Genre::getId))
                .map(Genre::getId)
                .map(genreStorage::findGenreById)
                .collect(Collectors.toSet());
        film.setGenres(genres);
    }

    private void uploadFilmGenres(Film film) {
        Set<Genre> genres = new TreeSet<>(Comparator.comparing(Genre::getId));
        genres.addAll(genreStorage.findFilmGenres(film.getId()));
        film.setGenres(genres);
    }

    private void uploadLikes(Film film) {
        String sql = "select user_id from likes where film_id = ?";
        List<Long> likes = jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getLong("user_id"), film.getId()
        );
        film.setLikes(new HashSet<>(likes));
    }
}
