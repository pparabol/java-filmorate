package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    @Qualifier("userDbStorage")
    private final UserStorage userStorage;

    public void addToFriends(long userId, long friendId) {
        if (userId == friendId) {
            throw new ValidationException("Нельзя добавить себя в друзья");
        }
        userStorage.addFriend(userId, friendId);
    }

    public void removeFromFriends(long userId, long friendId) {
        if (userId == friendId) {
            throw new ValidationException("Нельзя удалить себя из друзей");
        }
        userStorage.removeFriend(userId, friendId);
    }

    public List<User> findFriends(long id) {
        return userStorage.findFriends(id);
    }

    public List<User> findCommonFriends(long userId, long otherUserId) {
        return userStorage.findCommonFriends(userId, otherUserId);
    }

    public User create(User user) {
        checkName(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        checkName(user);
        return userStorage.update(user);
    }

    public User findUserById(long id) {
        return userStorage.findUserById(id);
    }

    public List<User> findAll() {
        return userStorage.findAll();
    }

    private void checkName(User user) {
        if (StringUtils.isBlank(user.getName())) {
            user.setName(user.getLogin());
        }
    }
}
