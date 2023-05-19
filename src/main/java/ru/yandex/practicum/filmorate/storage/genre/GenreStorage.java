package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

public interface GenreStorage {

    Collection<Genre> findAll();

    Genre findGenreById(int id);

    Collection<Genre> findFilmGenres(long filmId);

    void addFilmGenres(long filmId, Collection<Genre> genres);

    void updateFilmGenres(long filmId, Collection<Genre> genres);
}
