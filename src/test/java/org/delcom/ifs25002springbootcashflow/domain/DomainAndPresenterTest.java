package org.delcom.ifs25002springbootcashflow.domain;

import org.delcom.ifs25002springbootcashflow.domain.entity.PasswordHasher;
import org.delcom.ifs25002springbootcashflow.domain.entity.Role;
import org.delcom.ifs25002springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25002springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25002springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25002springbootcashflow.domain.entity.User;
import org.delcom.ifs25002springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25002springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25002springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25002springbootcashflow.framework.component.FinancePresenter;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DomainAndPresenterTest {

    @Test
    void testUserEntityAndRoles() {
        User user = new User("Trifena", "trifena@delcom.org", "hash123", Role.USER);
        assertNull(user.getId());
        assertEquals("Trifena", user.getName());
        assertEquals("trifena@delcom.org", user.getEmail());
        assertEquals("hash123", user.getPasswordHash());
        assertEquals(Role.USER, user.getRole());
        assertNull(user.getPhotoPath());
        assertNotNull(user.getCreatedAt());
        assertFalse(user.isAdmin());

        user.setId(10L);
        user.setName("Trifena Admin");
        user.setEmail("admin@delcom.org");
        user.setPasswordHash("newhash");
        user.setRole(Role.ADMIN);
        user.setPhotoPath("avatar.png");

        assertEquals(10L, user.getId());
        assertEquals("Trifena Admin", user.getName());
        assertEquals("admin@delcom.org", user.getEmail());
        assertEquals("newhash", user.getPasswordHash());
        assertEquals(Role.ADMIN, user.getRole());
        assertEquals("avatar.png", user.getPhotoPath());
        assertTrue(user.isAdmin());
    }

    @Test
    void testTransactionEntityAndEnums() {
        Transaction tx = new Transaction(1L, "Gaji", "Bulan November", TransactionType.INCOME, 5000000.0, "slip.png");
        assertNull(tx.getId());
        assertEquals(1L, tx.getUserId());
        assertEquals("Gaji", tx.getTitle());
        assertEquals("Bulan November", tx.getDescription());
        assertEquals(TransactionType.INCOME, tx.getType());
        assertEquals(5000000.0, tx.getAmount());
        assertEquals("slip.png", tx.getAttachmentPath());
        assertNotNull(tx.getCreatedAt());

        LocalDateTime customTime = LocalDateTime.of(2026, 11, 17, 10, 0);
        tx.setId(99L);
        tx.setUserId(2L);
        tx.setTitle("Belanja");
        tx.setDescription("Kebutuhan");
        tx.setType(TransactionType.EXPENSE);
        tx.setAmount(150000.0);
        tx.setAttachmentPath("struk.jpg");
        tx.setCreatedAt(customTime);

        assertEquals(99L, tx.getId());
        assertEquals(2L, tx.getUserId());
        assertEquals("Belanja", tx.getTitle());
        assertEquals("Kebutuhan", tx.getDescription());
        assertEquals(TransactionType.EXPENSE, tx.getType());
        assertEquals(150000.0, tx.getAmount());
        assertEquals("struk.jpg", tx.getAttachmentPath());
        assertEquals(customTime, tx.getCreatedAt());

        assertEquals("Pemasukan", TransactionType.INCOME.getLabel());
        assertEquals("Pengeluaran", TransactionType.EXPENSE.getLabel());
        assertEquals("Terbaru", SortOption.NEWEST.getLabel());
        assertEquals("Terlama", SortOption.OLDEST.getLabel());
        assertEquals("Judul A-Z", SortOption.TITLE_ASC.getLabel());
        assertEquals("Judul Z-A", SortOption.TITLE_DESC.getLabel());
        assertEquals("Nominal Terbesar", SortOption.AMOUNT_HIGH.getLabel());
        assertEquals("Nominal Terkecil", SortOption.AMOUNT_LOW.getLabel());
    }

    @Test
    void testPasswordHasherAndExceptions() {
        new PasswordHasher();
        String hashed = PasswordHasher.hash("rahasia123");
        assertTrue(PasswordHasher.verify("rahasia123", hashed));
        assertFalse(PasswordHasher.verify("salah", hashed));

        try (MockedStatic<MessageDigest> mocked = Mockito.mockStatic(MessageDigest.class)) {
            mocked.when(() -> MessageDigest.getInstance("SHA-256"))
                    .thenThrow(new NoSuchAlgorithmException("No SHA-256"));
            assertThrows(IllegalStateException.class, () -> PasswordHasher.hash("abc"));
        }

        DomainException d1 = new DomainException("err");
        DomainException d2 = new DomainException("err", new RuntimeException());
        EntityNotFoundException enf = new EntityNotFoundException("User", 5L);
        ValidationException ve = new ValidationException("invalid");

        assertEquals("err", d1.getMessage());
        assertNotNull(d2.getCause());
        assertTrue(enf.getMessage().contains("5"));
        assertEquals("invalid", ve.getMessage());
    }

    @Test
    void testFinancePresenter() {
        new FinancePresenter();
        assertTrue(FinancePresenter.formatCurrency(25000.0).contains("25.000"));
        assertTrue(FinancePresenter.formatCurrency(null).contains("0"));

        LocalDateTime dt = LocalDateTime.of(2026, 11, 17, 9, 30);
        assertNotEquals("-", FinancePresenter.formatDateTime(dt));
        assertEquals("-", FinancePresenter.formatDateTime(null));

        assertEquals("Pemasukan", FinancePresenter.formatTypeLabel(TransactionType.INCOME));
        assertEquals("-", FinancePresenter.formatTypeLabel(null));
    }
}