package org.delcom.ifs25002springbootcashflow.config;

import org.delcom.ifs25002springbootcashflow.adapter.repository.JdbcTransactionRepository;
import org.delcom.ifs25002springbootcashflow.adapter.repository.JdbcUserRepository;
import org.delcom.ifs25002springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25002springbootcashflow.domain.entity.PasswordHasher;
import org.delcom.ifs25002springbootcashflow.domain.entity.Role;
import org.delcom.ifs25002springbootcashflow.domain.entity.User;
import org.delcom.ifs25002springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25002springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25002springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25002springbootcashflow.usecase.TransactionUseCase;
import org.delcom.ifs25002springbootcashflow.usecase.UserUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Locale;

@Configuration
public class AppConfig {

    private static final String DDL_USERS = """
            CREATE TABLE IF NOT EXISTS users (
                id BIGSERIAL PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                email VARCHAR(255) NOT NULL UNIQUE,
                password_hash VARCHAR(255) NOT NULL,
                role VARCHAR(50) NOT NULL,
                photo_path VARCHAR(255),
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """;

    private static final String DDL_TRANSACTIONS = """
            CREATE TABLE IF NOT EXISTS transactions (
                id BIGSERIAL PRIMARY KEY,
                user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                title VARCHAR(255) NOT NULL,
                description TEXT,
                type VARCHAR(50) NOT NULL,
                amount DOUBLE PRECISION NOT NULL DEFAULT 0.0,
                attachment_path VARCHAR(255),
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """;

    @Value("${app.upload.dir:uploads}")
    private String storageDirectory;

    @Value("${ROOT_ADMIN_NAME:Administrator}")
    private String defaultAdminName;

    @Value("${ROOT_ADMIN_EMAIL:admin@delcom.org}")
    private String defaultAdminEmail;

    @Value("${ROOT_ADMIN_PASSWORD:admin123}")
    private String defaultAdminPassword;

    @Bean
    public FileStorage fileStorage() {
        return new FileStorage(storageDirectory);
    }

    @Bean
    public IUserRepository userRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcUserRepository(jdbcTemplate);
    }

    @Bean
    public ITransactionRepository transactionRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcTransactionRepository(jdbcTemplate);
    }

    @Bean
    public AuthUseCase authUseCase(IUserRepository userRepository) {
        return new AuthUseCase(userRepository);
    }

    @Bean
    public UserUseCase userUseCase(IUserRepository userRepository) {
        return new UserUseCase(userRepository);
    }

    @Bean
    public TransactionUseCase transactionUseCase(ITransactionRepository transactionRepository) {
        return new TransactionUseCase(transactionRepository);
    }

    @Bean
    public CommandLineRunner databaseInitializer(JdbcTemplate jdbcTemplate, IUserRepository userRepository) {
        return args -> {
            jdbcTemplate.execute(DDL_USERS);
            jdbcTemplate.execute(DDL_TRANSACTIONS);

            String normalizedEmail = defaultAdminEmail.trim().toLowerCase(Locale.ROOT);
            if (!userRepository.existsByEmail(normalizedEmail)) {
                User rootAdmin = new User(
                        defaultAdminName,
                        normalizedEmail,
                        PasswordHasher.hash(defaultAdminPassword),
                        Role.ADMIN
                );
                userRepository.save(rootAdmin);
            }
        };
    }
}