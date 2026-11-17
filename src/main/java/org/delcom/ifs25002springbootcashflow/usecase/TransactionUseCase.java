package org.delcom.ifs25002springbootcashflow.usecase;

import org.delcom.ifs25002springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25002springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25002springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25002springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25002springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25002springbootcashflow.domain.repository.ITransactionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TransactionUseCase {

    private final ITransactionRepository transactionRepo;

    public TransactionUseCase(ITransactionRepository transactionRepository) {
        this.transactionRepo = transactionRepository;
    }

    public Transaction create(Long userId, String title, String description,
                              TransactionType type, Double amount, String attachmentPath) {
        validateTransactionFields(title, type, amount);

        String cleanDescription = Objects.requireNonNullElse(description, "").trim();
        Transaction entry = new Transaction(
                null,
                userId,
                title.trim(),
                cleanDescription,
                type,
                amount,
                attachmentPath,
                LocalDateTime.now()
        );
        return transactionRepo.save(entry);
    }

    public Transaction findById(Long id) {
        return transactionRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaction", id));
    }

    public List<Transaction> findByUserId(Long userId) {
        return transactionRepo.findByUserId(userId);
    }

    public List<Transaction> search(Long userId, String keyword, TransactionType type, SortOption sortOption) {
        SortOption effectiveSort = Objects.requireNonNullElse(sortOption, SortOption.NEWEST);
        return transactionRepo.search(userId, keyword, type, effectiveSort);
    }

    public Transaction update(Long id, Long requestingUserId, String title,
                              String description, TransactionType type, Double amount, String attachmentPath) {
        validateTransactionFields(title, type, amount);

        Transaction existing = findById(id);
        ensureTransactionOwner(existing, requestingUserId);

        existing.setTitle(title.trim());
        existing.setDescription(Objects.requireNonNullElse(description, "").trim());
        existing.setType(type);
        existing.setAmount(amount);
        Optional.ofNullable(attachmentPath).ifPresent(existing::setAttachmentPath);

        return transactionRepo.save(existing);
    }

    public void delete(Long id, Long requestingUserId) {
        Transaction existing = findById(id);
        ensureTransactionOwner(existing, requestingUserId);
        transactionRepo.deleteById(id);
    }

    public Double getTotalIncome(Long userId) {
        return transactionRepo.getTotalIncome(userId);
    }

    public Double getTotalExpense(Long userId) {
        return transactionRepo.getTotalExpense(userId);
    }

    public Double getBalance(Long userId) {
        return transactionRepo.getBalance(userId);
    }

    private void validateTransactionFields(String title, TransactionType type, Double amount) {
        if (title == null || title.isBlank()) {
            throw new ValidationException("Judul transaksi wajib diisi.");
        }
        if (type == null) {
            throw new ValidationException("Jenis transaksi harus dipilih.");
        }
        if (amount == null || amount <= 0) {
            throw new ValidationException("Nominal transaksi harus bernilai positif.");
        }
    }

    private void ensureTransactionOwner(Transaction transaction, Long actorUserId) {
        if (!transaction.getUserId().equals(actorUserId)) {
            throw new ValidationException("Anda tidak memiliki akses untuk memodifikasi transaksi ini.");
        }
    }
}