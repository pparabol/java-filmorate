package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    public void addToFriends(long userId, long friendId) {
        if (userId == friendId) {
            throw new ValidationException("Нельзя добавить себя в друзья");
        }
        User user = userStorage.findUserById(userId);
        User friend = userStorage.findUserById(friendId);

        user.addFriend(friendId);
        friend.addFriend(userId);
        log.debug("Пользователь № {} добавил в друзья пользователя № {}", userId, friendId);
    }

    public void removeFromFriends(long userId, long friendId) {
        if (userId == friendId) {
            throw new ValidationException("Нельзя удалить себя из друзей");
        }
        User user = userStorage.findUserById(userId);
        User friend = userStorage.findUserById(friendId);

        user.removeFriend(friendId);
        friend.removeFriend(userId);
        log.debug("Пользователь № {} удалил из друзей пользователя № {}", userId, friendId);
    }

    public List<User> findFriends(long id) {
        User user = userStorage.findUserById(id);
        if (user.getFriends().isEmpty()) {
            return new ArrayList<>();
        }
        return user.getFriends().stream()
                .map(userStorage::findUserById)
                .collect(Collectors.toList());
    }

    public List<User> findCommonFriends(long userId, long otherUserId) {
        User user = userStorage.findUserById(userId);
        User otherUser = userStorage.findUserById(otherUserId);
        if (user.getFriends().isEmpty() || otherUser.getFriends().isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> commonFriends = user.getFriends().stream()
                .filter(otherUser.getFriends()::contains)
                .collect(Collectors.toSet());
        return commonFriends.stream()
                .map(userStorage::findUserById)
                .collect(Collectors.toList());
    }

    public User create(User user) {
        user.setId();
        user.setName();
        log.debug("Создан пользователь: {}", user);
        return userStorage.create(user);
    }

    public User update(User user) {
        user.setName();
        return userStorage.update(user);
    }

    public User findUserById(long id) {
        return userStorage.findUserById(id);
    }

    public List<User> findAll() {
        return userStorage.findAll();
    }
}
