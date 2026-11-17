package org.delcom.ifs25002springbootcashflow.adapter.repository;

import org.delcom.ifs25002springbootcashflow.domain.entity.User;
import org.delcom.ifs25002springbootcashflow.domain.repository.IUserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryUserRepository implements IUserRepository {

    private final Map<Long, User> userStore = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(sequence.getAndIncrement());
        }
        userStore.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(userStore.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(userStore.values());
    }

    @Override
    public void deleteById(Long id) {
        userStore.remove(id);
    }

    @Override
    public boolean existsById(Long id) {
        return userStore.containsKey(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userStore.values().stream()
                .filter(account -> matchesEmail(account, email))
                .findFirst();
    }

    @Override
    public boolean existsByEmail(String email) {
        return userStore.values().stream()
                .anyMatch(account -> matchesEmail(account, email));
    }

    private boolean matchesEmail(User account, String targetEmail) {
        return targetEmail != null && account.getEmail().equalsIgnoreCase(targetEmail.trim());
    }
}