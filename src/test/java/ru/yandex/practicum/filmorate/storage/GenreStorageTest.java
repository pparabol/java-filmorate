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
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreStorageTest {

    @Autowired
    private final GenreStorage genreStorage;
    @Autowired
    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;
    @Autowired
    private final JdbcTemplate jdbcTemplate;
    private Film film;

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
    }

    @AfterEach
    public void tearDown() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "film_genres", "films");
    }

    @Test
    public void testFindAll() {
        Collection<Genre> actual = genreStorage.findAll();

        assertThat(actual)
                .asList()
                .hasSize(6)
                .doesNotContainNull()
                .doesNotHaveDuplicates();
    }

    @Test
    public void testFindGenreById() {
        Genre actual = genreStorage.findGenreById(1);

        assertThat(actual)
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "Комедия");
    }

    @Test
    public void testFindFilmGenres() {
        film.setGenres(Set.of(new Genre(1, "Комедия")));
        Film createdFilm = filmStorage.create(film);

        Collection<Genre> actual = genreStorage.findFilmGenres(createdFilm.getId());

        assertThat(actual)
                .asList()
                .hasSize(1)
                .containsAll(film.getGenres());
    }

    @Test
    public void testAddFilmGenres() {
        Film createdFilm = filmStorage.create(film);
        Set<Genre> genres = Set.of(new Genre(2, "Драма"));
        createdFilm.setGenres(genres);

        genreStorage.addFilmGenres(createdFilm.getId(), createdFilm.getGenres());

        Collection<Genre> actual = genreStorage.findFilmGenres(createdFilm.getId());

        assertThat(actual)
                .asList()
                .hasSize(1)
                .containsAll(genres);
    }

    @Test
    public void testUpdateFilmGenres() {
        film.setGenres(Set.of(new Genre(1, "Комедия")));
        Film createdFilm = filmStorage.create(film);

        Set<Genre> genres = Set.of(new Genre(2, "Драма"), new Genre(3, "Мультфильм"));
        createdFilm.setGenres(genres);

        genreStorage.updateFilmGenres(createdFilm.getId(), createdFilm.getGenres());

        Collection<Genre> actual = genreStorage.findFilmGenres(createdFilm.getId());

        assertThat(actual)
                .asList()
                .hasSize(2)
                .containsAll(genres);
    }

}
