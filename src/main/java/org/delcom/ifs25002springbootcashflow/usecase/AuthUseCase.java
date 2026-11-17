package org.delcom.ifs25002springbootcashflow.usecase;

import org.delcom.ifs25002springbootcashflow.domain.entity.PasswordHasher;
import org.delcom.ifs25002springbootcashflow.domain.entity.Role;
import org.delcom.ifs25002springbootcashflow.domain.entity.User;
import org.delcom.ifs25002springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25002springbootcashflow.domain.repository.IUserRepository;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class AuthUseCase {

    private final IUserRepository userRepo;

    public AuthUseCase(IUserRepository userRepository) {
        this.userRepo = userRepository;
    }

    public User register(String name, String email, String password, Role role) {
        validateRegistrationData(name, email, password);

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (userRepo.existsByEmail(normalizedEmail)) {
            throw new ValidationException("Alamat email sudah terdaftar.");
        }

        Role assignedRole = Objects.requireNonNullElse(role, Role.USER);
        User newAccount = new User(
                name.trim(),
                normalizedEmail,
                PasswordHasher.hash(password),
                assignedRole
        );
        return userRepo.save(newAccount);
    }

    public Optional<User> login(String email, String password) {
        if (email == null || password == null) {
            return Optional.empty();
        }
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        return userRepo.findByEmail(normalizedEmail)
                .filter(account -> PasswordHasher.verify(password, account.getPasswordHash()));
    }

    private void validateRegistrationData(String name, String email, String password) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Nama pengguna wajib diisi.");
        }
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email pengguna wajib diisi.");
        }
        if (password == null || password.length() < 6) {
            throw new ValidationException("Password harus terdiri dari minimal 6 karakter.");
        }
    }
}