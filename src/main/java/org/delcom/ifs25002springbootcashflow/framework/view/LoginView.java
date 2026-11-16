package org.delcom.ifs25002springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.delcom.ifs25002springbootcashflow.domain.User;
import org.delcom.ifs25002springbootcashflow.framework.security.SessionManager;
import org.delcom.ifs25002springbootcashflow.usecase.AuthUseCase;

import java.util.Optional;

@Route("login")
@PageTitle("Login - Cashflow 11S25002")
public class LoginView extends VerticalLayout {

    public LoginView(AuthUseCase authUseCase) {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        VerticalLayout card = new VerticalLayout();
        card.setWidth("380px");
        card.setPadding(true);
        card.getStyle()
                .set("border", "1px solid #e5e7eb")
                .set("border-radius", "10px")
                .set("background", "#ffffff");

        H2 title = new H2("Masuk ke Cashflow");
        Paragraph subtitle = new Paragraph("Gunakan akun terdaftar untuk mengelola transaksi.");

        TextField usernameField = new TextField("Username / Email");
        usernameField.setId("username");
        usernameField.setWidthFull();
        usernameField.setPlaceholder("admin@delcom.org");

        PasswordField passwordField = new PasswordField("Password");
        passwordField.setId("password");
        passwordField.setWidthFull();

        Button loginButton = new Button("Login", e -> {
            Optional<User> user = authUseCase.login(usernameField.getValue(), passwordField.getValue());
            if (user.isPresent()) {
                SessionManager.setCurrentUser(user.get());
                UI.getCurrent().navigate("");
            } else {
                Notification notif = Notification.show("Username atau password salah.", 3000, Notification.Position.TOP_CENTER);
                notif.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.setWidthFull();

        RouterLink registerLink = new RouterLink("Belum punya akun? Daftar di sini", RegisterView.class);

        card.add(title, subtitle, usernameField, passwordField, loginButton, registerLink);
        add(card);
    }
}