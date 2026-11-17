package org.delcom.ifs25002springbootcashflow.usecase;

import org.delcom.ifs25002springbootcashflow.adapter.repository.InMemoryTransactionRepository;
import org.delcom.ifs25002springbootcashflow.adapter.repository.InMemoryUserRepository;
import org.delcom.ifs25002springbootcashflow.domain.entity.Role;
import org.delcom.ifs25002springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25002springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25002springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25002springbootcashflow.domain.entity.User;
import org.delcom.ifs25002springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25002springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UseCasesTest {

    @Test
    void testAuthUseCase() {
        InMemoryUserRepository userRepo = new InMemoryUserRepository();
        AuthUseCase authUseCase = new AuthUseCase(userRepo);

        assertThrows(ValidationException.class, () -> authUseCase.register(null, "a@b.com", "123456", Role.USER));
        assertThrows(ValidationException.class, () -> authUseCase.register("  ", "a@b.com", "123456", Role.USER));
        assertThrows(ValidationException.class, () -> authUseCase.register("User", null, "123456", Role.USER));
        assertThrows(ValidationException.class, () -> authUseCase.register("User", "  ", "123456", Role.USER));
        assertThrows(ValidationException.class, () -> authUseCase.register("User", "a@b.com", null, Role.USER));
        assertThrows(ValidationException.class, () -> authUseCase.register("User", "a@b.com", "123", Role.USER));

        User created = authUseCase.register("Budi", "BUDI@mail.com", "secret12", null);
        assertEquals(Role.USER, created.getRole());
        assertEquals("budi@mail.com", created.getEmail());

        assertThrows(ValidationException.class, () -> authUseCase.register("Budi 2", "budi@mail.com", "secret12", Role.ADMIN));

        assertTrue(authUseCase.login(null, "secret12").isEmpty());
        assertTrue(authUseCase.login("budi@mail.com", null).isEmpty());
        assertTrue(authUseCase.login("budi@mail.com", "wrong").isEmpty());
        assertTrue(authUseCase.login("budi@mail.com", "secret12").isPresent());
    }

    @Test
    void testUserUseCase() {
        InMemoryUserRepository userRepo = new InMemoryUserRepository();
        AuthUseCase authUseCase = new AuthUseCase(userRepo);
        UserUseCase userUseCase = new UserUseCase(userRepo);

        User u = authUseCase.register("Siti", "siti@mail.com", "pass123", Role.USER);
        assertEquals("Siti", userUseCase.findById(u.getId()).getName());
        assertThrows(EntityNotFoundException.class, () -> userUseCase.findById(999L));
        assertEquals(1, userUseCase.findAll().size());

        assertThrows(ValidationException.class, () -> userUseCase.updateProfile(u.getId(), null, null));
        assertThrows(ValidationException.class, () -> userUseCase.updateProfile(u.getId(), " ", null));

        User updated1 = userUseCase.updateProfile(u.getId(), "Siti Baru", null);
        assertNull(updated1.getPhotoPath());

        User updated2 = userUseCase.updateProfile(u.getId(), "Siti Foto", "foto.jpg");
        assertEquals("foto.jpg", updated2.getPhotoPath());

        assertThrows(ValidationException.class, () -> userUseCase.changePassword(u.getId(), "pass123", null));
        assertThrows(ValidationException.class, () -> userUseCase.changePassword(u.getId(), "pass123", "123"));
        assertThrows(ValidationException.class, () -> userUseCase.changePassword(u.getId(), "salah", "baru123"));

        userUseCase.changePassword(u.getId(), "pass123", "baru123");
        assertTrue(authUseCase.login("siti@mail.com", "baru123").isPresent());
    }

    @Test
    void testTransactionUseCase() {
        InMemoryTransactionRepository txRepo = new InMemoryTransactionRepository();
        TransactionUseCase txUseCase = new TransactionUseCase(txRepo);

        assertThrows(ValidationException.class, () -> txUseCase.create(1L, null, "d", TransactionType.INCOME, 100.0, null));
        assertThrows(ValidationException.class, () -> txUseCase.create(1L, " ", "d", TransactionType.INCOME, 100.0, null));
        assertThrows(ValidationException.class, () -> txUseCase.create(1L, "T", "d", null, 100.0, null));
        assertThrows(ValidationException.class, () -> txUseCase.create(1L, "T", "d", TransactionType.INCOME, null, null));
        assertThrows(ValidationException.class, () -> txUseCase.create(1L, "T", "d", TransactionType.INCOME, 0.0, null));

        Transaction tx = txUseCase.create(1L, "Proyek", null, TransactionType.INCOME, 1000.0, "a.png");
        assertEquals(1000.0, txUseCase.getTotalIncome(1L));
        assertEquals(0.0, txUseCase.getTotalExpense(1L));
        assertEquals(1000.0, txUseCase.getBalance(1L));
        assertEquals(1, txUseCase.findByUserId(1L).size());
        assertEquals(1, txUseCase.search(1L, null, null, null).size());
        assertEquals(1, txUseCase.search(1L, "Proyek", TransactionType.INCOME, SortOption.NEWEST).size());

        assertThrows(EntityNotFoundException.class, () -> txUseCase.findById(999L));
        assertThrows(ValidationException.class, () ->
                txUseCase.update(tx.getId(), 2L, "Hack", "d", TransactionType.INCOME, 500.0, null));

        Transaction updated1 = txUseCase.update(tx.getId(), 1L, "Proyek Rev", "Catatan", TransactionType.EXPENSE, 400.0, null);
        assertEquals("a.png", updated1.getAttachmentPath());

        Transaction updated2 = txUseCase.update(tx.getId(), 1L, "Proyek Final", null, TransactionType.EXPENSE, 450.0, "b.png");
        assertEquals("b.png", updated2.getAttachmentPath());

        assertThrows(ValidationException.class, () -> txUseCase.delete(tx.getId(), 2L));
        txUseCase.delete(tx.getId(), 1L);
        assertEquals(0, txUseCase.findByUserId(1L).size());
    }
}