package org.delcom.ifs25002springbootcashflow;

import org.delcom.ifs25002springbootcashflow.adapter.repository.JdbcTransactionRepository;
import org.delcom.ifs25002springbootcashflow.adapter.repository.JdbcUserRepository;
import org.delcom.ifs25002springbootcashflow.config.AppConfig;
import org.delcom.ifs25002springbootcashflow.config.DotEnv;
import org.delcom.ifs25002springbootcashflow.domain.entity.Role;
import org.delcom.ifs25002springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25002springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25002springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25002springbootcashflow.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JdbcAndAppIntegrationTest {

    private JdbcTemplate createH2JdbcTemplate() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.h2.Driver");
        ds.setUrl("jdbc:h2:mem:testdb_" + System.nanoTime() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        ds.setUsername("sa");
        ds.setPassword("");
        JdbcTemplate jdbc = new JdbcTemplate(ds);

        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id BIGSERIAL PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    email VARCHAR(255) NOT NULL UNIQUE,
                    password_hash VARCHAR(255) NOT NULL,
                    role VARCHAR(50) NOT NULL,
                    photo_path VARCHAR(255),
                    created_at TIMESTAMP
                )
                """);

        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id BIGSERIAL PRIMARY KEY,
                    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                    title VARCHAR(255) NOT NULL,
                    description TEXT,
                    type VARCHAR(50) NOT NULL,
                    amount DOUBLE PRECISION NOT NULL DEFAULT 0.0,
                    attachment_path VARCHAR(255),
                    created_at TIMESTAMP
                )
                """);
        return jdbc;
    }

    @Test
    void testJdbcUserAndTransactionRepositories() {
        JdbcTemplate jdbc = createH2JdbcTemplate();
        JdbcUserRepository userRepo = new JdbcUserRepository(jdbc);
        JdbcTransactionRepository txRepo = new JdbcTransactionRepository(jdbc);

        User u1 = userRepo.save(new User("Andi", "andi@delcom.org", "hash1", Role.USER));
        assertNotNull(u1.getId());
        assertTrue(userRepo.existsById(u1.getId()));
        assertFalse(userRepo.existsById(999L));
        assertTrue(userRepo.existsByEmail("ANDI@delcom.org"));
        assertFalse(userRepo.existsByEmail("none@delcom.org"));
        assertEquals("Andi", userRepo.findById(u1.getId()).orElseThrow().getName());
        assertTrue(userRepo.findByEmail("andi@delcom.org").isPresent());

        u1.setName("Andi Edited");
        userRepo.save(u1);
        assertEquals("Andi Edited", userRepo.findAll().get(0).getName());

        Transaction t1 = txRepo.save(new Transaction(u1.getId(), "Gaji", "Bulanan", TransactionType.INCOME, 2000.0, "a.png"));
        Transaction t2 = txRepo.save(new Transaction(u1.getId(), "Listrik", "Token", TransactionType.EXPENSE, 500.0, null));
        assertNotNull(t1.getId());
        assertTrue(txRepo.existsById(t1.getId()));
        assertFalse(txRepo.existsById(999L));
        assertEquals("Gaji", txRepo.findById(t1.getId()).orElseThrow().getTitle());
        assertEquals(2, txRepo.findAll().size());
        assertEquals(2, txRepo.findByUserId(u1.getId()).size());

        t1.setTitle("Gaji Pokok");
        txRepo.save(t1);
        assertEquals("Gaji Pokok", txRepo.findById(t1.getId()).orElseThrow().getTitle());

        assertEquals(2000.0, txRepo.getTotalIncome(u1.getId()));
        assertEquals(500.0, txRepo.getTotalExpense(u1.getId()));
        assertEquals(1500.0, txRepo.getBalance(u1.getId()));

        assertEquals(2, txRepo.search(u1.getId(), null, null, null).size());
        assertEquals(2, txRepo.search(u1.getId(), "  ", null, SortOption.OLDEST).size());
        assertEquals(1, txRepo.search(u1.getId(), "token", TransactionType.EXPENSE, SortOption.TITLE_ASC).size());
        assertEquals(1, txRepo.search(u1.getId(), "pokok", TransactionType.INCOME, SortOption.TITLE_DESC).size());
        assertEquals(2, txRepo.search(u1.getId(), null, null, SortOption.AMOUNT_HIGH).size());
        assertEquals(2, txRepo.search(u1.getId(), null, null, SortOption.AMOUNT_LOW).size());

        // Uji baris dengan created_at null
        jdbc.update("INSERT INTO users (name, email, password_hash, role, created_at) VALUES (?, ?, ?, ?, NULL)",
                "NullTs", "nullts@delcom.org", "h", "USER");
        User nullTsUser = userRepo.findByEmail("nullts@delcom.org").orElseThrow();
        assertNotNull(nullTsUser.getCreatedAt());

        jdbc.update("INSERT INTO transactions (user_id, title, description, type, amount, created_at) VALUES (?, ?, ?, ?, ?, NULL)",
                nullTsUser.getId(), "NullTx", "Desc", "INCOME", 100.0);
        List<Transaction> nullTsTxs = txRepo.findByUserId(nullTsUser.getId());
        assertEquals(1, nullTsTxs.size());
        assertNotNull(nullTsTxs.get(0).getCreatedAt());

        txRepo.deleteById(t2.getId());
        assertFalse(txRepo.existsById(t2.getId()));

        userRepo.deleteById(u1.getId());
        assertFalse(userRepo.existsById(u1.getId()));
    }

    @Test
    void testAppConfigAndDotEnv(@TempDir Path tempDir) throws Exception {
        new DotEnv();
        DotEnv.load();

        Path envPath = Path.of(".env");
        boolean existed = Files.exists(envPath);
        String backup = existed ? Files.readString(envPath) : null;

        try {
            Files.writeString(envPath, """
                    # Komentar
                    INVALID_LINE_WITHOUT_EQUALS

                    TEST_DOTENV_KEY="nilai_test"
                    PATH=ignored_because_already_in_env
                    """);
            System.setProperty("EXISTING_PROP_KEY", "sudah_ada");
            Files.writeString(envPath, Files.readString(envPath) + "EXISTING_PROP_KEY=baru\n");

            DotEnv.load();
            assertEquals("nilai_test", System.getProperty("TEST_DOTENV_KEY"));
            assertEquals("sudah_ada", System.getProperty("EXISTING_PROP_KEY"));
        } finally {
            System.clearProperty("TEST_DOTENV_KEY");
            System.clearProperty("EXISTING_PROP_KEY");
            if (existed) {
                Files.writeString(envPath, backup);
            } else {
                Files.deleteIfExists(envPath);
            }
        }

        JdbcTemplate jdbc = createH2JdbcTemplate();
        AppConfig config = new AppConfig();
        var storageField = AppConfig.class.getDeclaredField("storageDirectory");
        storageField.setAccessible(true);
        storageField.set(config, tempDir.toString());

        var nameField = AppConfig.class.getDeclaredField("defaultAdminName");
        nameField.setAccessible(true);
        nameField.set(config, "Root Admin");

        var emailField = AppConfig.class.getDeclaredField("defaultAdminEmail");
        emailField.setAccessible(true);
        emailField.set(config, "root@delcom.org");

        var passField = AppConfig.class.getDeclaredField("defaultAdminPassword");
        passField.setAccessible(true);
        passField.set(config, "admin123");

        assertNotNull(config.fileStorage());
        var uRepo = config.userRepository(jdbc);
        var tRepo = config.transactionRepository(jdbc);
        assertNotNull(config.authUseCase(uRepo));
        assertNotNull(config.userUseCase(uRepo));
        assertNotNull(config.transactionUseCase(tRepo));

        CommandLineRunner runner = config.databaseInitializer(jdbc, uRepo);
        runner.run();
        assertTrue(uRepo.existsByEmail("root@delcom.org"));
        // Jalankan kedua kali untuk menutup branch existsByEmail == true
        runner.run();
        assertEquals(1, uRepo.findAll().size());
    }

    @Test
    void testApplicationMain() {
        new Ifs25002P5Pbo2026SpringbootCashflowApplication();
        Ifs25002P5Pbo2026SpringbootCashflowApplication.main(new String[]{
                "--server.port=0",
                "--spring.datasource.url=jdbc:h2:mem:mainboot;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                "--spring.datasource.driver-class-name=org.h2.Driver",
                "--spring.datasource.username=sa",
                "--spring.datasource.password="
        });
    }
}