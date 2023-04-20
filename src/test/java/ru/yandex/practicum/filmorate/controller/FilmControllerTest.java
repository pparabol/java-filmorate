package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmControllerTest {

    private Validator validator;
    private FilmController controller;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        controller = new FilmController(new FilmService(new InMemoryFilmStorage(), new InMemoryUserStorage()));
    }

    @Test
    void emptyNameShouldFailValidation() {
        Film film = new Film("",
                "description",
                LocalDate.of(2003, 5, 5),
                3
        );
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @Test
    void tooLargeDescriptionShouldFailValidation() {
        Film film = new Film("film",
                "extremely large description extremely large description extremely large description " +
                        "extremely large description extremely large description extremely large description " +
                        "extremely large description extremely large description",
                LocalDate.of(2003, 5, 5),
                3
        );
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenInvalidReleaseDate() {
        Film film = new Film("film",
                "description",
                LocalDate.of(1000, 5, 5),
                3
        );
        final ValidationException exception = assertThrows(ValidationException.class,
                () -> controller.create(film)
        );
        assertEquals("Дата релиза не может быть раньше 28.12.1895", exception.getMessage());
    }

    @Test
    void negativeDurationShouldFailValidation() {
        Film film = new Film("film",
                "description",
                LocalDate.of(2005, 5, 5),
                -300
        );
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingFilm() {
        Film film = new Film("film",
                "description",
                LocalDate.of(2005, 5, 5),
                100
        );
        film.generateId();
        final FilmNotFoundException exception = assertThrows(FilmNotFoundException.class,
                () -> controller.update(film)
        );
        assertEquals("Фильм № " + film.getId() + " не найден", exception.getMessage());
        assertTrue(controller.findAll().isEmpty());
    }
}
