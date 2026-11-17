package org.delcom.ifs25002springbootcashflow.adapter.repository;

import org.delcom.ifs25002springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25002springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25002springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25002springbootcashflow.domain.repository.ITransactionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class JdbcTransactionRepository implements ITransactionRepository {

    private final JdbcTemplate jdbc;

    public JdbcTransactionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    @Override
    public Transaction save(Transaction transaction) {
        return transaction.getId() == null ? insertTransaction(transaction) : updateTransaction(transaction);
    }

    private Transaction insertTransaction(Transaction tx) {
        String sql = "INSERT INTO transactions (user_id, title, description, type, amount, attachment_path, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime timestamp = Objects.requireNonNullElseGet(tx.getCreatedAt(), LocalDateTime::now);

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, tx.getUserId());
            ps.setString(2, tx.getTitle());
            ps.setString(3, tx.getDescription());
            ps.setString(4, tx.getType().name());
            ps.setDouble(5, tx.getAmount());
            ps.setString(6, tx.getAttachmentPath());
            ps.setTimestamp(7, Timestamp.valueOf(timestamp));
            return ps;
        }, keyHolder);

        if (keyHolder.getKeys() != null && keyHolder.getKeys().containsKey("id")) {
            tx.setId(((Number) keyHolder.getKeys().get("id")).longValue());
        } else if (keyHolder.getKey() != null) {
            tx.setId(keyHolder.getKey().longValue());
        }
        return tx;
    }

    private Transaction updateTransaction(Transaction tx) {
        String sql = "UPDATE transactions SET title = ?, description = ?, type = ?, amount = ?, attachment_path = ? WHERE id = ?";
        jdbc.update(
                sql,
                tx.getTitle(),
                tx.getDescription(),
                tx.getType().name(),
                tx.getAmount(),
                tx.getAttachmentPath(),
                tx.getId()
        );
        return tx;
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        String sql = "SELECT id, user_id, title, description, type, amount, attachment_path, created_at FROM transactions WHERE id = ?";
        return jdbc.query(sql, this::mapRow, id).stream().findFirst();
    }

    @Override
    public List<Transaction> findAll() {
        String sql = "SELECT id, user_id, title, description, type, amount, attachment_path, created_at FROM transactions ORDER BY id DESC";
        return jdbc.query(sql, this::mapRow);
    }

    @Override
    public void deleteById(Long id) {
        jdbc.update("DELETE FROM transactions WHERE id = ?", id);
    }

    @Override
    public boolean existsById(Long id) {
        Integer count = jdbc.queryForObject("SELECT COUNT(1) FROM transactions WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public List<Transaction> findByUserId(Long userId) {
        String sql = "SELECT id, user_id, title, description, type, amount, attachment_path, created_at FROM transactions WHERE user_id = ? ORDER BY id DESC";
        return jdbc.query(sql, this::mapRow, userId);
    }

    @Override
    public List<Transaction> search(Long userId, String keyword, TransactionType type, SortOption sortOption) {
        StringBuilder query = new StringBuilder(
                "SELECT id, user_id, title, description, type, amount, attachment_path, created_at FROM transactions WHERE user_id = ?"
        );
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (type != null) {
            query.append(" AND type = ?");
            params.add(type.name());
        }

        if (keyword != null && !keyword.isBlank()) {
            query.append(" AND (LOWER(title) LIKE LOWER(?) OR LOWER(description) LIKE LOWER(?))");
            String likeTerm = "%" + keyword.trim() + "%";
            params.add(likeTerm);
            params.add(likeTerm);
        }

        query.append(buildOrderClause(sortOption));
        return jdbc.query(query.toString(), this::mapRow, params.toArray());
    }

    @Override
    public Double getTotalIncome(Long userId) {
        return querySumByType(userId, TransactionType.INCOME);
    }

    @Override
    public Double getTotalExpense(Long userId) {
        return querySumByType(userId, TransactionType.EXPENSE);
    }

    @Override
    public Double getBalance(Long userId) {
        return getTotalIncome(userId) - getTotalExpense(userId);
    }

    private Double querySumByType(Long userId, TransactionType type) {
        String sql = "SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE user_id = ? AND type = ?";
        Double total = jdbc.queryForObject(sql, Double.class, userId, type.name());
        return Objects.requireNonNullElse(total, 0.0);
    }

    private String buildOrderClause(SortOption sortOption) {
        SortOption effective = Objects.requireNonNullElse(sortOption, SortOption.NEWEST);
        return switch (effective) {
            case NEWEST -> " ORDER BY id DESC";
            case OLDEST -> " ORDER BY id ASC";
            case TITLE_ASC -> " ORDER BY LOWER(title) ASC";
            case TITLE_DESC -> " ORDER BY LOWER(title) DESC";
            case AMOUNT_HIGH -> " ORDER BY amount DESC, id DESC";
            case AMOUNT_LOW -> " ORDER BY amount ASC, id DESC";
        };
    }

    private Transaction mapRow(ResultSet rs, int rowNum) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
        return new Transaction(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("title"),
                rs.getString("description"),
                TransactionType.valueOf(rs.getString("type")),
                rs.getDouble("amount"),
                rs.getString("attachment_path"),
                createdAt
        );
    }
}