package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MpaStorageTest {

    @Autowired
    private final MpaStorage mpaStorage;

    @Test
    public void testFindAll() {
        List<Mpa> actual = mpaStorage.findAll();

        assertThat(actual)
                .asList()
                .hasSize(5)
                .doesNotContainNull()
                .doesNotHaveDuplicates();
    }

    @Test
    public void testFindMpaById() {
        Mpa actual = mpaStorage.findMpaById(1);

        assertThat(actual)
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "G");
    }
}
