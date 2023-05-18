package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new ConcurrentHashMap<>();

    @Override
    public Film create(Film film) {
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        checkFilm(film.getId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film findFilmById(long id) {
        checkFilm(id);
        return films.get(id);
    }

    @Override
    public List<Film> findPopular(int count) {
        return films.values().stream()
                .sorted((f1, f2) -> f2.getLikes().size() - f1.getLikes().size())
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public void like(long filmId, long userId) {

    }

    @Override
    public void unlike(long filmId, long userId) {

    }

    private void checkFilm(long id) {
        if (!films.containsKey(id)) {
            throw new FilmNotFoundException(String.format("Фильм № %d не найден", id));
        }
    }
}