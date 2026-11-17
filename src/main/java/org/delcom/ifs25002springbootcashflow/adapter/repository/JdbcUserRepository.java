package org.delcom.ifs25002springbootcashflow.adapter.repository;

import org.delcom.ifs25002springbootcashflow.domain.entity.Role;
import org.delcom.ifs25002springbootcashflow.domain.entity.User;
import org.delcom.ifs25002springbootcashflow.domain.repository.IUserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class JdbcUserRepository implements IUserRepository {

    private final JdbcTemplate jdbc;

    public JdbcUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    @Override
    public User save(User user) {
        return user.getId() == null ? insertRecord(user) : updateRecord(user);
    }

    private User insertRecord(User user) {
        String insertSql = "INSERT INTO users (name, email, password_hash, role, photo_path, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime createdTime = Objects.requireNonNullElseGet(user.getCreatedAt(), LocalDateTime::now);

        jdbc.update(conn -> {
            PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            stmt.setString(4, user.getRole().name());
            stmt.setString(5, user.getPhotoPath());
            stmt.setTimestamp(6, Timestamp.valueOf(createdTime));
            return stmt;
        }, keyHolder);

        if (keyHolder.getKeys() != null && keyHolder.getKeys().containsKey("id")) {
            user.setId(((Number) keyHolder.getKeys().get("id")).longValue());
        } else if (keyHolder.getKey() != null) {
            user.setId(keyHolder.getKey().longValue());
        }
        return user;
    }

    private User updateRecord(User user) {
        String updateSql = "UPDATE users SET name = ?, email = ?, password_hash = ?, role = ?, photo_path = ? WHERE id = ?";
        jdbc.update(
                updateSql,
                user.getName(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole().name(),
                user.getPhotoPath(),
                user.getId()
        );
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        String querySql = "SELECT id, name, email, password_hash, role, photo_path, created_at FROM users WHERE id = ?";
        return jdbc.query(querySql, this::mapRowToUser, id).stream().findFirst();
    }

    @Override
    public List<User> findAll() {
        String querySql = "SELECT id, name, email, password_hash, role, photo_path, created_at FROM users ORDER BY id ASC";
        return jdbc.query(querySql, this::mapRowToUser);
    }

    @Override
    public void deleteById(Long id) {
        jdbc.update("DELETE FROM users WHERE id = ?", id);
    }

    @Override
    public boolean existsById(Long id) {
        Integer total = jdbc.queryForObject("SELECT COUNT(1) FROM users WHERE id = ?", Integer.class, id);
        return total != null && total > 0;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String querySql = "SELECT id, name, email, password_hash, role, photo_path, created_at FROM users WHERE LOWER(email) = LOWER(?)";
        return jdbc.query(querySql, this::mapRowToUser, email).stream().findFirst();
    }

    @Override
    public boolean existsByEmail(String email) {
        Integer total = jdbc.queryForObject("SELECT COUNT(1) FROM users WHERE LOWER(email) = LOWER(?)", Integer.class, email);
        return total != null && total > 0;
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        Timestamp createdTs = rs.getTimestamp("created_at");
        LocalDateTime createdAt = createdTs != null ? createdTs.toLocalDateTime() : LocalDateTime.now();
        return new User(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password_hash"),
                Role.valueOf(rs.getString("role")),
                rs.getString("photo_path"),
                createdAt
        );
    }
}