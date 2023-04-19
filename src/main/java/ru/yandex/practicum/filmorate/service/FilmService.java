package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public void like(long filmId, long userId) {
        Film film = filmStorage.findFilmById(filmId);
        User user = userStorage.findUserById(userId);
        film.addLike(userId);
        log.debug("Пользователь № {} поставил лайк фильму № {}", userId, filmId);
    }

    public void unlike(long filmId, long userId) {
        Film film = filmStorage.findFilmById(filmId);
        User user = userStorage.findUserById(userId);
        film.removeLike(userId);
        log.debug("Пользователь № {} убрал лайк с фильма № {}", userId, filmId);
    }

    public List<Film> findPopular(int count) {
        List<Film> films = filmStorage.findAll().stream()
                .sorted((f1, f2) -> f2.getLikes().size() - f1.getLikes().size())
                .limit(count)
                .collect(Collectors.toList());
        System.out.println(films);
        return films;
    }

    public Film create(Film film) {
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.warn("Попытка добавить фильм с недопустимой датой релиза: {}", film);
            throw new ValidationException("Дата релиза не может быть раньше 28.12.1895");
        }
        film.setId();
        log.debug("Добавлен фильм: {}", film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public Film findFilmById(long id) {
        return filmStorage.findFilmById(id);
    }

    public List<Film> findAll() {
        return filmStorage.findAll();
    }
}
