package org.delcom.ifs25002springbootcashflow.framework.auth;

import com.vaadin.flow.server.VaadinSession;
import org.delcom.ifs25002springbootcashflow.domain.entity.User;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthSession {

    private static final String SESSION_ACCOUNT_ATTR = "AUTH_USER_ACCOUNT";

    public void login(User user) {
        VaadinSession.getCurrent().setAttribute(SESSION_ACCOUNT_ATTR, user);
    }

    public void logout() {
        VaadinSession currentSession = VaadinSession.getCurrent();
        currentSession.setAttribute(SESSION_ACCOUNT_ATTR, null);
        currentSession.close();
    }

    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(VaadinSession.getCurrent())
                .map(session -> (User) session.getAttribute(SESSION_ACCOUNT_ATTR));
    }

    public boolean isLoggedIn() {
        return getCurrentUser().isPresent();
    }

    public boolean isAdmin() {
        return getCurrentUser().map(User::isAdmin).orElse(false);
    }
}