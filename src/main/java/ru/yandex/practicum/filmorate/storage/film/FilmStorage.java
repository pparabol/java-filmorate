package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    Film create(Film film);

    Film update(Film film);

    List<Film> findAll();

    Film findFilmById(long id);

    List<Film> findPopular(int count);

    void like(long filmId, long userId);

    void unlike(long filmId, long userId);
}
