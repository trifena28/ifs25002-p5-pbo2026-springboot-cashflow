package org.delcom.ifs25002springbootcashflow.adapter.repository;

import org.delcom.ifs25002springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25002springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25002springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25002springbootcashflow.domain.repository.ITransactionRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryTransactionRepository implements ITransactionRepository {

    private final Map<Long, Transaction> records = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    @Override
    public Transaction save(Transaction transaction) {
        if (transaction.getId() == null) {
            transaction.setId(idSequence.getAndIncrement());
        }
        records.put(transaction.getId(), transaction);
        return transaction;
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        return Optional.ofNullable(records.get(id));
    }

    @Override
    public List<Transaction> findAll() {
        return new ArrayList<>(records.values());
    }

    @Override
    public void deleteById(Long id) {
        records.remove(id);
    }

    @Override
    public boolean existsById(Long id) {
        return records.containsKey(id);
    }

    @Override
    public List<Transaction> findByUserId(Long userId) {
        return records.values().stream()
                .filter(item -> Objects.equals(item.getUserId(), userId))
                .sorted(Comparator.comparing(Transaction::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> search(Long userId, String keyword, TransactionType type, SortOption sortOption) {
        String normalizedQuery = (keyword != null && !keyword.isBlank())
                ? keyword.trim().toLowerCase(Locale.ROOT)
                : null;

        return records.values().stream()
                .filter(item -> Objects.equals(item.getUserId(), userId))
                .filter(item -> type == null || item.getType() == type)
                .filter(item -> matchesKeyword(item, normalizedQuery))
                .sorted(resolveComparator(sortOption))
                .collect(Collectors.toList());
    }

    @Override
    public Double getTotalIncome(Long userId) {
        return sumByType(userId, TransactionType.INCOME);
    }

    @Override
    public Double getTotalExpense(Long userId) {
        return sumByType(userId, TransactionType.EXPENSE);
    }

    @Override
    public Double getBalance(Long userId) {
        return getTotalIncome(userId) - getTotalExpense(userId);
    }

    private double sumByType(Long userId, TransactionType targetType) {
        return records.values().stream()
                .filter(item -> Objects.equals(item.getUserId(), userId))
                .filter(item -> item.getType() == targetType)
                .mapToDouble(item -> Objects.requireNonNullElse(item.getAmount(), 0.0))
                .sum();
    }

    private boolean matchesKeyword(Transaction item, String normalizedQuery) {
        if (normalizedQuery == null) {
            return true;
        }
        boolean titleMatch = item.getTitle() != null
                && item.getTitle().toLowerCase(Locale.ROOT).contains(normalizedQuery);
        boolean descMatch = item.getDescription() != null
                && item.getDescription().toLowerCase(Locale.ROOT).contains(normalizedQuery);
        return titleMatch || descMatch;
    }

    private Comparator<Transaction> resolveComparator(SortOption option) {
        SortOption effective = Objects.requireNonNullElse(option, SortOption.NEWEST);
        return switch (effective) {
            case NEWEST -> Comparator.comparing(Transaction::getId, Comparator.nullsLast(Comparator.reverseOrder()));
            case OLDEST -> Comparator.comparing(Transaction::getId, Comparator.nullsLast(Comparator.naturalOrder()));
            case TITLE_ASC -> Comparator.comparing(t -> t.getTitle().toLowerCase(Locale.ROOT));
            case TITLE_DESC -> Comparator.comparing((Transaction t) -> t.getTitle().toLowerCase(Locale.ROOT)).reversed();
            case AMOUNT_HIGH -> Comparator.comparing(Transaction::getAmount, Comparator.nullsLast(Comparator.reverseOrder()));
            case AMOUNT_LOW -> Comparator.comparing(Transaction::getAmount, Comparator.nullsLast(Comparator.naturalOrder()));
        };
    }
}