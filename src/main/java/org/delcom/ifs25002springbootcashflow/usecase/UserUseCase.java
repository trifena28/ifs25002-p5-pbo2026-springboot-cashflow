package org.delcom.ifs25002springbootcashflow.usecase;

import org.delcom.ifs25002springbootcashflow.domain.entity.PasswordHasher;
import org.delcom.ifs25002springbootcashflow.domain.entity.User;
import org.delcom.ifs25002springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25002springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25002springbootcashflow.domain.repository.IUserRepository;

import java.util.List;
import java.util.Optional;

public class UserUseCase {

    private final IUserRepository userRepo;

    public UserUseCase(IUserRepository userRepository) {
        this.userRepo = userRepository;
    }

    public User findById(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));
    }

    public List<User> findAll() {
        return userRepo.findAll();
    }

    public User updateProfile(Long id, String name, String photoPath) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Nama pengguna tidak boleh kosong.");
        }
        User account = findById(id);
        account.setName(name.trim());
        Optional.ofNullable(photoPath).ifPresent(account::setPhotoPath);
        return userRepo.save(account);
    }

    public void changePassword(Long id, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new ValidationException("Password baru harus minimal 6 karakter.");
        }
        User account = findById(id);
        if (!PasswordHasher.verify(oldPassword, account.getPasswordHash())) {
            throw new ValidationException("Password lama tidak sesuai.");
        }
        account.setPasswordHash(PasswordHasher.hash(newPassword));
        userRepo.save(account);
    }
}