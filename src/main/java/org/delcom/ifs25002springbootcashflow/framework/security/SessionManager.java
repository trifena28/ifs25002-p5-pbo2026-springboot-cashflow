package org.delcom.ifs25002springbootcashflow.framework.security;

import com.vaadin.flow.server.VaadinSession;
import org.delcom.ifs25002springbootcashflow.domain.User;

public class SessionManager {
    private static final String USER_KEY = "CURRENT_LOGGED_IN_USER";

    public static void setCurrentUser(User user) {
        VaadinSession.getCurrent().setAttribute(USER_KEY, user);
    }

    public static User getCurrentUser() {
        if (VaadinSession.getCurrent() == null) return null;
        return (User) VaadinSession.getCurrent().getAttribute(USER_KEY);
    }

    public static boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    public static void logout() {
        if (VaadinSession.getCurrent() != null) {
            VaadinSession.getCurrent().setAttribute(USER_KEY, null);
        }
    }
}