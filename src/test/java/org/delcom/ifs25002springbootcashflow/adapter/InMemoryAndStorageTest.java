package org.delcom.ifs25002springbootcashflow.adapter;

import org.delcom.ifs25002springbootcashflow.adapter.repository.InMemoryTransactionRepository;
import org.delcom.ifs25002springbootcashflow.adapter.repository.InMemoryUserRepository;
import org.delcom.ifs25002springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25002springbootcashflow.domain.entity.Role;
import org.delcom.ifs25002springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25002springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25002springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25002springbootcashflow.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryAndStorageTest {

    @Test
    void testInMemoryUserRepository() {
        InMemoryUserRepository repo = new InMemoryUserRepository();
        User u1 = repo.save(new User("Alice", "alice@mail.com", "h1", Role.USER));
        assertNotNull(u1.getId());
        assertTrue(repo.existsById(u1.getId()));
        assertFalse(repo.existsById(999L));
        assertEquals("Alice", repo.findById(u1.getId()).orElseThrow().getName());
        assertEquals(1, repo.findAll().size());

        u1.setName("Alice Updated");
        repo.save(u1);
        assertEquals("Alice Updated", repo.findById(u1.getId()).orElseThrow().getName());

        assertTrue(repo.findByEmail("ALICE@mail.com").isPresent());
        assertTrue(repo.findByEmail(null).isEmpty());
        assertTrue(repo.findByEmail("other@mail.com").isEmpty());
        assertTrue(repo.existsByEmail("alice@mail.com"));
        assertFalse(repo.existsByEmail(null));

        repo.deleteById(u1.getId());
        assertFalse(repo.existsById(u1.getId()));
    }

    @Test
    void testInMemoryTransactionRepository() {
        InMemoryTransactionRepository repo = new InMemoryTransactionRepository();
        Transaction t1 = repo.save(new Transaction(1L, "Beta Income", "Bonus", TransactionType.INCOME, 500.0, null));
        Transaction t2 = repo.save(new Transaction(1L, "Alpha Expense", "Makan siang", TransactionType.EXPENSE, 200.0, null));
        Transaction t3 = repo.save(new Transaction(2L, "Other User", null, TransactionType.INCOME, 900.0, null));
        Transaction t4 = repo.save(new Transaction(null, 1L, "Null Desc", null, TransactionType.INCOME, 300.0, null, LocalDateTime.now()));

        assertTrue(repo.existsById(t1.getId()));
        assertFalse(repo.existsById(999L));
        assertEquals(4, repo.findAll().size());
        assertEquals(3, repo.findByUserId(1L).size());

        t1.setTitle("Beta Income Edited");
        repo.save(t1);
        assertEquals("Beta Income Edited", repo.findById(t1.getId()).orElseThrow().getTitle());

        assertEquals(800.0, repo.getTotalIncome(1L));
        assertEquals(200.0, repo.getTotalExpense(1L));
        assertEquals(600.0, repo.getBalance(1L));

        assertEquals(3, repo.search(1L, null, null, null).size());
        assertEquals(3, repo.search(1L, "   ", null, SortOption.OLDEST).size());
        assertEquals(1, repo.search(1L, "makan", TransactionType.EXPENSE, SortOption.TITLE_ASC).size());
        assertEquals(1, repo.search(1L, "beta", TransactionType.INCOME, SortOption.TITLE_DESC).size());
        assertEquals(0, repo.search(1L, "tidakada", null, SortOption.AMOUNT_HIGH).size());
        assertEquals(3, repo.search(1L, null, null, SortOption.AMOUNT_LOW).size());

        repo.deleteById(t3.getId());
        assertFalse(repo.existsById(t3.getId()));
        assertNotNull(t4);
    }

    @Test
    void testFileStorage(@TempDir Path tempDir) throws IOException {
        FileStorage storage = new FileStorage(tempDir.toString());

        assertThrows(IllegalArgumentException.class, () -> storage.store(new ByteArrayInputStream(new byte[0]), null));
        assertThrows(IllegalArgumentException.class, () -> storage.store(new ByteArrayInputStream(new byte[0]), "   "));

        byte[] data = "hello image".getBytes(StandardCharsets.UTF_8);
        String savedWithExt = storage.store(new ByteArrayInputStream(data), "photo.png");
        assertTrue(savedWithExt.endsWith(".png"));
        assertArrayEquals(data, storage.loadAsBytes(savedWithExt));

        String savedNoExt = storage.store(new ByteArrayInputStream(data), "noext");
        assertFalse(savedNoExt.contains("."));
        assertArrayEquals(data, storage.loadAsBytes(savedNoExt));

        assertNull(storage.loadAsBytes("missing.png"));
        assertNull(storage.loadAsBytes("."));

        storage.delete(savedWithExt);
        assertNull(storage.loadAsBytes(savedWithExt));
        storage.delete(null);
        storage.delete("  ");
        Files.createDirectory(tempDir.resolve("non_empty_dir")); Files.writeString(tempDir.resolve("non_empty_dir/f.txt"), "x"); storage.delete("non_empty_dir");

        InputStream failingStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("disk error");
            }
        };
        assertThrows(IllegalStateException.class, () -> storage.store(failingStream, "err.jpg"));

        Path blockingFile = tempDir.resolve("blocked_file");
        Files.writeString(blockingFile, "block");
        assertThrows(IllegalStateException.class, () -> new FileStorage(blockingFile.toString()));
    }
}