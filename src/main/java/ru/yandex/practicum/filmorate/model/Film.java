package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {

    private static long nextId = 1;

    private long id;
    @NotBlank(message = "Название не может быть пустым")
    private final String name;
    @Size(max = 200, message = "Максимальный размер описания - 200 символов")
    private final String description;
    @NotNull(message = "Необходимо указать дату релиза")
    private final LocalDate releaseDate;
    @Positive(message = "Длительность не может быть отрицательной")
    private final long duration;
    private Set<Long> likes = new HashSet<>();

    public void generateId() {
        if (id == 0) {
            id = nextId;
            nextId++;
        }
    }

    public void addLike(long id) {
        likes.add(id);
    }

    public void removeLike(long id) {
        likes.remove(id);
    }
}
