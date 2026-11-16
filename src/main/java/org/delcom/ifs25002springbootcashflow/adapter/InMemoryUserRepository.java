package org.delcom.ifs25002springbootcashflow.adapter;

import org.delcom.ifs25002springbootcashflow.domain.User;
import org.delcom.ifs25002springbootcashflow.domain.UserRepository;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class InMemoryUserRepository implements UserRepository {
    private final Map<String, User> users = Collections.synchronizedMap(new LinkedHashMap<>());

    @Override
    public User save(User user) {
        users.put(user.getUsername().toLowerCase(Locale.ROOT).trim(), user);
        return user;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return Optional.ofNullable(users.get(username.toLowerCase(Locale.ROOT).trim()));
    }
}