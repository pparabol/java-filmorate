package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Component("userDbStorage")
@Slf4j
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public User create(User user) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");

        long userId = simpleJdbcInsert.executeAndReturnKey(user.toMap()).longValue();
        user.setId(userId);

        log.debug("Создан пользователь: {}", user);

        return user;
    }

    @Override
    public User update(User user) {
        String sql = "update users set email = ?, login = ?, name = ?, " +
                "birthday = ? where user_id = ?";
        int responseNumber = jdbcTemplate.update(
                sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );

        if (responseNumber != 0) {
            log.debug("Обновлён пользователь: {}", user);
            return user;
        } else {
            log.warn("Пользователь с ID {} не найден", user.getId());
            throw new UserNotFoundException(String.format("Пользователь № %d не найден", user.getId()));
        }
    }

    @Override
    public List<User> findAll() {
        String sql = "select * from users";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeUser(rs));
    }

    @Override
    public User findUserById(long id) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("select * from users where user_id = ?", id);

        if (userRows.next()) {
            User user = new User(
                    userRows.getString("email"),
                    userRows.getString("login"),
                    Objects.requireNonNull(userRows.getDate("birthday")).toLocalDate()
            );
            user.setId(userRows.getLong("user_id"));
            user.setName(userRows.getString("name"));
            uploadFriends(user);
            return user;
        } else {
            log.warn("Пользователь с ID {} не найден", id);
            throw new UserNotFoundException(String.format("Пользователь № %d не найден", id));
        }
    }

    @Override
    public void addFriend(long userId, long friendId) {
        findUserById(userId);
        findUserById(friendId);

        log.debug("Пользователь № {} отправил заявку в друзья пользователю № {}", userId, friendId);

        String sql;
        if (isFriends(userId, friendId)) {
            sql = "update friends set is_accepted = true " +
                    "where user_id = ? and friend_id = ?";
            log.debug("Дружба стала взаимной у пользователей № {} и № {}", userId, friendId);
        } else {
            sql = "insert into friends (user_id, friend_id, is_accepted) " +
                    "values (?, ?, false)";
        }
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        findUserById(userId);
        findUserById(friendId);

        String sql;
        if (!isFriends(friendId, userId)) {
            sql = "delete from friends where user_id = ? and friend_id = ?";
        } else {
            sql = "update friends set is_accepted = false " +
                    "where user_id = ? and friend_id = ?";
        }
        jdbcTemplate.update(sql, userId, friendId);
        log.debug("Пользователь № {} удалил из друзей пользователя № {}", userId, friendId);
    }

    @Override
    public List<User> findFriends(long id) {
        String sql = "select * from users where user_id in " +
                "(select friend_id from friends where user_id = ?)";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeUser(rs), id);
    }

    @Override
    public List<User> findCommonFriends(long id, long otherId) {
        String sql = "select * from users where user_id in " +
                "(select friend_id from friends where user_id = ?) " +
                "intersect " +
                "select * from users where user_id in " +
                "(select friend_id from friends where user_id = ?)";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeUser(rs), id, otherId);
    }

    private User makeUser(ResultSet rs) throws SQLException {
        User user = new User(
                rs.getString("email"),
                rs.getString("login"),
                Objects.requireNonNull(rs.getDate("birthday")).toLocalDate()
        );
        user.setId(rs.getLong("user_id"));
        user.setName(rs.getString("name"));
        uploadFriends(user);
        return user;
    }

    private void uploadFriends(User user) {
        String sql = "select friend_id from friends where user_id = ?";
        List<Long> friends = jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getLong("friend_id"), user.getId()
        );
        user.setFriends(new HashSet<>(friends));
    }

    private boolean isFriends(long userId, long otherId) {
        SqlRowSet friendsRows = jdbcTemplate.queryForRowSet(
                "select * from friends where user_id = ? and friend_id = ?",
                userId, otherId
        );
        return friendsRows.next();
    }
}
