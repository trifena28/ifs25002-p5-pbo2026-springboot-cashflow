package org.delcom.ifs25002springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.delcom.ifs25002springbootcashflow.usecase.AuthUseCase;

@Route("register")
@PageTitle("Daftar Akun - Cashflow 11S25002")
public class RegisterView extends VerticalLayout {

    public RegisterView(AuthUseCase authUseCase) {
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

        H2 title = new H2("Registrasi Akun Baru");

        TextField namaField = new TextField("Nama Lengkap");
        namaField.setWidthFull();

        TextField usernameField = new TextField("Username / Email");
        usernameField.setWidthFull();

        PasswordField passwordField = new PasswordField("Password");
        passwordField.setWidthFull();

        Button registerBtn = new Button("Daftar", e -> {
            try {
                authUseCase.register(namaField.getValue(), usernameField.getValue(), passwordField.getValue());
                Notification notif = Notification.show("Registrasi berhasil! Silakan login.", 3000, Notification.Position.TOP_CENTER);
                notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                UI.getCurrent().navigate("login");
            } catch (IllegalArgumentException ex) {
                Notification notif = Notification.show(ex.getMessage(), 3000, Notification.Position.TOP_CENTER);
                notif.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        registerBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerBtn.setWidthFull();

        RouterLink loginLink = new RouterLink("Sudah punya akun? Login", LoginView.class);

        card.add(title, namaField, usernameField, passwordField, registerBtn, loginLink);
        add(card);
    }
}