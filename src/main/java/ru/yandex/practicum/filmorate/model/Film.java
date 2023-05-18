package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.*;

@Data
public class Film {

    private long id;
    @NotBlank(message = "Название не может быть пустым")
    private final String name;
    @Size(max = 200, message = "Максимальный размер описания - 200 символов")
    private final String description;
    @NotNull(message = "Необходимо указать дату релиза")
    private final LocalDate releaseDate;
    @Positive(message = "Длительность не может быть отрицательной")
    private final long duration;
    private Mpa mpa;
    private Set<Long> likes = new HashSet<>();
    private Set<Genre> genres = new HashSet<>();

    public Film(long id, String name, String description, LocalDate releaseDate, long duration, Mpa mpa) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.mpa = mpa;
    }

    public void addLike(long id) {
        likes.add(id);
    }

    public void removeLike(long id) {
        likes.remove(id);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("name", name);
        values.put("description", description);
        values.put("release_date", releaseDate);
        values.put("duration", duration);
        values.put("mpa_id", mpa.getId());
        return values;
    }
}
