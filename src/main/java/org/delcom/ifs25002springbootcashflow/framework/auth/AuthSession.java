package org.delcom.ifs25002springbootcashflow.framework.auth;

import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WrappedSession;
import org.delcom.ifs25002springbootcashflow.domain.entity.User;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthSession {

    private static final String SESSION_ACCOUNT_ATTR = "AUTH_USER_ACCOUNT";

    public void login(User user) {
        VaadinSession vaadinSession = VaadinSession.getCurrent();
        if (vaadinSession != null) {
            vaadinSession.setAttribute(SESSION_ACCOUNT_ATTR, user);
        }
        WrappedSession httpSession = getWrappedSession();
        if (httpSession != null) {
            httpSession.setAttribute(SESSION_ACCOUNT_ATTR, user);
        }
    }

    public void logout() {
        VaadinSession currentSession = VaadinSession.getCurrent();
        if (currentSession != null) {
            currentSession.setAttribute(SESSION_ACCOUNT_ATTR, null);
            currentSession.close();
        }
        WrappedSession httpSession = getWrappedSession();
        if (httpSession != null) {
            httpSession.removeAttribute(SESSION_ACCOUNT_ATTR);
        }
    }

    public Optional<User> getCurrentUser() {
        VaadinSession vaadinSession = VaadinSession.getCurrent();
        if (vaadinSession != null) {
            User sessionUser = (User) vaadinSession.getAttribute(SESSION_ACCOUNT_ATTR);
            if (sessionUser != null) {
                return Optional.of(sessionUser);
            }
        }
        WrappedSession httpSession = getWrappedSession();
        if (httpSession != null) {
            User httpUser = (User) httpSession.getAttribute(SESSION_ACCOUNT_ATTR);
            if (httpUser != null) {
                if (vaadinSession != null) {
                    vaadinSession.setAttribute(SESSION_ACCOUNT_ATTR, httpUser);
                }
                return Optional.of(httpUser);
            }
        }
        return Optional.empty();
    }

    public boolean isLoggedIn() {
        return getCurrentUser().isPresent();
    }

    public boolean isAdmin() {
        return getCurrentUser().map(User::isAdmin).orElse(false);
    }

    private WrappedSession getWrappedSession() {
        VaadinRequest request = VaadinService.getCurrentRequest();
        return request != null ? request.getWrappedSession(false) : null;
    }
}