package org.delcom.ifs25002springbootcashflow.usecase;

import org.delcom.ifs25002springbootcashflow.domain.User;
import org.delcom.ifs25002springbootcashflow.domain.UserRepository;

import java.util.Optional;

public class AuthUseCase {
    private final UserRepository userRepository;

    public AuthUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String nama, String username, String password) {
        if (nama == null || nama.isBlank()) {
            throw new IllegalArgumentException("Nama wajib diisi.");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username / Email wajib diisi.");
        }
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("Password minimal 4 karakter.");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username / Email sudah terdaftar.");
        }
        return userRepository.save(new User(nama.trim(), username.trim(), password));
    }

    public Optional<User> login(String username, String password) {
        if (username == null || password == null) return Optional.empty();
        return userRepository.findByUsername(username)
                .filter(u -> u.getPassword().equals(password));
    }
}