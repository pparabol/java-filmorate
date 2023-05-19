package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new ConcurrentHashMap<>();

    @Override
    public User create(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        checkUser(user.getId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User findUserById(long id) {
        checkUser(id);
        return users.get(id);
    }

    @Override
    public void addFriend(long userId, long friendId) {
        User user = findUserById(userId);
        User friend = findUserById(friendId);

        friend.addFriend(userId);
        log.debug("Пользователь № {} отправил заявку в друзья пользователю № {}", userId, friendId);

        if (user.getFriends().contains(friendId)) {
            user.addFriend(friendId);
            log.debug("Дружба стала взаимной у пользователей № {} и № {}", userId, friendId);
        }
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        User user = findUserById(userId);
        findUserById(friendId);

        user.removeFriend(friendId);
        log.debug("Пользователь № {} удалил из друзей пользователя № {}", userId, friendId);
    }

    @Override
    public List<User> findFriends(long id) {
        User user = findUserById(id);
        if (user.getFriends().isEmpty()) {
            return new ArrayList<>();
        }
        return user.getFriends().stream()
                .map(this::findUserById)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findCommonFriends(long id, long otherId) {
        User user = findUserById(id);
        User otherUser = findUserById(otherId);
        if (user.getFriends().isEmpty() || otherUser.getFriends().isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> commonFriends = user.getFriends().stream()
                .filter(otherUser.getFriends()::contains)
                .collect(Collectors.toSet());
        return commonFriends.stream()
                .map(this::findUserById)
                .collect(Collectors.toList());
    }

    private void checkUser(long id) {
        if (!users.containsKey(id)) {
            throw new UserNotFoundException(String.format("Пользователь № %d не найден", id));
        }
    }
}
