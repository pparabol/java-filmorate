package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {

    private static long nextId = 1;

    private long id;
    @Email(message = "Некорректный email")
    private final String email;
    @NotBlank(message = "Логин не может быть пустым")
    private final String login;
    private String name;
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private final LocalDate birthday;
    private Set<Long> friends = new HashSet<>();

    public void generateId() {
        if (id == 0) {
            id = nextId;
            nextId++;
        }
    }

    public void addFriend(long id) {
        friends.add(id);
    }

    public void removeFriend(long id) {
        friends.remove(id);
    }
}
