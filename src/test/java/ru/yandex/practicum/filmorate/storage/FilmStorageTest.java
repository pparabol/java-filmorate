package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmStorageTest {

    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;
    private Film film;
    private User user;

    @BeforeEach
    public void setUp() {
        film = new Film(
                1,
                "Film",
                "description",
                LocalDate.now(),
                30,
                new Mpa(1, "G")
        );
        user = new User(
                "email@mail.ru",
                "login",
                LocalDate.now()
        );
    }

    @AfterEach
    public void tearDown() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate,
                "likes", "film_genres", "films", "users"
        );
    }

    @Test
    public void testCreate() {
        Film actual = filmStorage.create(film);

        assertThat(actual)
                .hasFieldOrPropertyWithValue("name", film.getName())
                .hasFieldOrPropertyWithValue("description", film.getDescription())
                .hasFieldOrPropertyWithValue("releaseDate", film.getReleaseDate())
                .hasFieldOrPropertyWithValue("duration", film.getDuration())
                .hasFieldOrPropertyWithValue("mpa", film.getMpa());
    }

    @Test
    public void testUpdate() {
        Film createdFilm = filmStorage.create(film);

        Mpa updatedMpa = new Mpa(2, "PG");
        Film updatedFilm = new Film(
                createdFilm.getId(),
                "Film",
                "updated",
                LocalDate.now(),
                30,
                updatedMpa
        );
        Set<Genre> updatedGenres = Set.of(new Genre(1, "Комедия"));
        updatedFilm.setGenres(updatedGenres);

        Film actual = filmStorage.update(updatedFilm);

        assertThat(actual)
                .hasFieldOrPropertyWithValue("description", "updated")
                .hasFieldOrPropertyWithValue("mpa", updatedMpa)
                .hasFieldOrPropertyWithValue("genres", updatedGenres);
    }

    @Test
    public void testFindAll() {
        Film otherFilm = new Film(
                2,
                "Film1",
                "description1",
                LocalDate.now(),
                80,
                new Mpa(4, "R")
        );

        filmStorage.create(film);
        filmStorage.create(otherFilm);

        List<Film> actual = filmStorage.findAll();

        assertThat(actual)
                .asList()
                .contains(film, otherFilm);
    }

    @Test
    public void testFindFilmById() {
        Film created = filmStorage.create(film);

        Film actual = filmStorage.findFilmById(created.getId());

        assertThat(actual)
                .hasFieldOrPropertyWithValue("id", created.getId())
                .hasFieldOrPropertyWithValue("name", created.getName());
    }

    @Test
    public void testFindPopular() {
        Film film1 = new Film(
                2,
                "Film",
                "testPopular",
                LocalDate.now(),
                80,
                new Mpa(4, "R")
        );

        film1.setLikes(Set.of(1L));

        filmStorage.create(film);
        Film createdFilm = filmStorage.create(film1);
        User createdUser = userStorage.create(user);

        filmStorage.like(createdFilm.getId(), createdUser.getId());

        Film popularFilm = filmStorage.findFilmById(createdFilm.getId());

        List<Film> actual = filmStorage.findPopular(10);

        assertThat(actual)
                .asList()
                .hasSize(2)
                .contains(film, popularFilm);
    }

    @Test
    public void testLike() {
        Film createdFilm = filmStorage.create(film);
        User createdUser = userStorage.create(user);

        createdFilm.setLikes(Set.of(1L));

        filmStorage.like(film.getId(), createdUser.getId());

        Film actual = filmStorage.findFilmById(createdFilm.getId());

        assertThat(actual)
                .hasFieldOrPropertyWithValue("likes", createdFilm.getLikes());

    }

    @Test
    public void testUnlike() {
        Film createdFilm = filmStorage.create(film);
        User createdUser = userStorage.create(user);
        filmStorage.like(createdFilm.getId(), createdUser.getId());
        filmStorage.unlike(createdFilm.getId(), createdUser.getId());

        Film actual = filmStorage.findFilmById(createdFilm.getId());

        assertThat(new ArrayList<>(actual.getLikes()))
                .asList()
                .isEmpty();
    }
}
