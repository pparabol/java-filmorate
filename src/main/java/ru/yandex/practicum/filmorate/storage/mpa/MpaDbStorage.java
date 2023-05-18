package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.MpaNotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Mpa> findAll() {
        String sql = "select * from mpa";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeMpa(rs));
    }

    @Override
    public Mpa findMpaById(int id) {
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("select * from mpa where mpa_id = ?", id);

        if(mpaRows.next()) {
            return new Mpa(mpaRows.getInt("mpa_id"), mpaRows.getString("name"));
        } else {
            throw new MpaNotFoundException(String.format("Рейтинг № %d не найден", id));
        }
    }

    private Mpa makeMpa(ResultSet rs) throws SQLException {
        return new Mpa(rs.getInt("mpa_id"), rs.getString("name"));
    }
}
