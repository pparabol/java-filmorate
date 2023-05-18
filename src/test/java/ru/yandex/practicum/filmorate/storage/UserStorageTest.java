package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserStorageTest {

    @Qualifier("userDbStorage")
    private final UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;
    private User user;
    private User otherUser;

    @BeforeEach
    public void setUp() {
        user = new User(
                "email@mail.ru",
                "login",
                LocalDate.now()
        );
        otherUser = new User(
                "user@mail.ru",
                "IamUser",
                LocalDate.now()
        );
    }

    @AfterEach
    public void tearDown() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "friends", "users");
    }

    @Test
    public void testCreate() {
        User actual = userStorage.create(user);

        assertThat(actual)
                .hasFieldOrPropertyWithValue("email", user.getEmail())
                .hasFieldOrPropertyWithValue("login", user.getLogin())
                .hasFieldOrPropertyWithValue("birthday", user.getBirthday());
    }

    @Test
    public void testUpdate() {
        User testUser = userStorage.create(user);

        User updatedUser = new User(
                "update@mail.ru",
                "updated",
                LocalDate.now()
        );
        updatedUser.setId(testUser.getId());

        User actual = userStorage.update(updatedUser);

        assertThat(actual)
                .hasFieldOrPropertyWithValue("email", updatedUser.getEmail())
                .hasFieldOrPropertyWithValue("login", updatedUser.getLogin());

    }

    @Test
    public void testFindAll() {
        User createdUser = userStorage.create(user);
        User createdOtherUser = userStorage.create(otherUser);

        List<User> actual = userStorage.findAll();

        assertThat(actual)
                .asList()
                .contains(createdUser, createdOtherUser);
    }

    @Test
    public void testFindUserById() {
        User testUser = userStorage.create(user);

        User actual = userStorage.findUserById(testUser.getId());

        assertThat(actual)
                .hasFieldOrPropertyWithValue("email", user.getEmail())
                .hasFieldOrPropertyWithValue("login", user.getLogin());
    }

    @Test
    public void testAddFriend() {
        User testUser = userStorage.create(user);
        User friend = userStorage.create(otherUser);

        userStorage.addFriend(testUser.getId(), friend.getId(), false);

        User userWithFriend = userStorage.findUserById(testUser.getId());
        User userWithoutFriend = userStorage.findUserById(friend.getId());

        assertThat(new ArrayList<>(userWithFriend.getFriends()))
                .asList()
                .hasSize(1)
                .contains(userWithoutFriend.getId());

        assertThat(new ArrayList<>(userWithoutFriend.getFriends()))
                .asList()
                .isEmpty();
    }

    @Test
    public void testRemoveFriend() {
        User testUser = userStorage.create(user);
        User friend = userStorage.create(otherUser);
        userStorage.addFriend(testUser.getId(), friend.getId(), false);

        userStorage.removeFriend(testUser.getId(), friend.getId(), true);

        User userWithoutFriend = userStorage.findUserById(testUser.getId());

        assertThat(new ArrayList<>(userWithoutFriend.getFriends()))
                .asList()
                .isEmpty();
    }

    @Test
    public void testFindFriends() {
        User testUser = userStorage.create(user);
        User friend = userStorage.create(new User(
                "friend@mail.ru",
                "friend",
                LocalDate.now()
        ));
        User otherFriend = userStorage.create(otherUser);

        userStorage.addFriend(testUser.getId(), friend.getId(), false);
        userStorage.addFriend(testUser.getId(), otherFriend.getId(), false);

        List<User> actual = userStorage.findFriends(testUser.getId());

        assertThat(actual)
                .asList()
                .hasSize(2)
                .contains(friend, otherFriend);
    }

    @Test
    public void testFindCommonFriends() {
        User testUser = userStorage.create(user);
        User friend = userStorage.create(new User(
                "friend@mail.ru",
                "friend",
                LocalDate.now()
        ));
        User otherFriend = userStorage.create(otherUser);

        userStorage.addFriend(testUser.getId(), friend.getId(), false);
        userStorage.addFriend(otherFriend.getId(), friend.getId(), false);

        List<User> actual = userStorage.findCommonFriends(testUser.getId(), otherFriend.getId());

        assertThat(actual)
                .asList()
                .hasSize(1)
                .contains(friend);
    }
}
