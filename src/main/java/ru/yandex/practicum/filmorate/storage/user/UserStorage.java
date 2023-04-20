package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    User create(User user);

    User update(User user);

    List<User> findAll();

    User findUserById(long id);

    List<User> findFriends(long id);

    List<User> findCommonFriends(long id, long otherId);
}
