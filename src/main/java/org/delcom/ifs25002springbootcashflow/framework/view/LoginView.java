package org.delcom.ifs25002springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.delcom.ifs25002springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25002springbootcashflow.usecase.AuthUseCase;

@Route("login")
@PageTitle("Masuk - Aplikasi Cashflow")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthUseCase authUseCase;
    private final AuthSession authSession;

    public LoginView(AuthUseCase authUseCase, AuthSession authSession) {
        this.authUseCase = authUseCase;
        this.authSession = authSession;

        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        H2 heading = new H2("Login Cashflow");

        TextField emailInput = new TextField("Alamat Email");
        emailInput.setId("login-username");
        emailInput.setWidth("330px");
        emailInput.setRequiredIndicatorVisible(true);

        PasswordField passwordInput = new PasswordField("Kata Sandi");
        passwordInput.setId("login-password");
        passwordInput.setWidth("330px");
        passwordInput.setRequiredIndicatorVisible(true);

        Button submitButton = new Button("Masuk", event -> authenticate(emailInput.getValue(), passwordInput.getValue()));
        submitButton.setId("login-submit");
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitButton.setWidth("330px");

        RouterLink signUpLink = new RouterLink("Belum memiliki akun? Daftar sekarang", RegisterView.class);

        VerticalLayout cardBox = new VerticalLayout(heading, emailInput, passwordInput, submitButton, signUpLink);
        cardBox.setWidth("380px");
        cardBox.setAlignItems(FlexComponent.Alignment.CENTER);
        add(cardBox);
    }

    private void authenticate(String email, String password) {
        authUseCase.login(email, password).ifPresentOrElse(
                account -> {
                    authSession.login(account);
                    getUI().ifPresent(ui -> ui.navigate(""));
                },
                () -> Notification.show("Email atau kata sandi tidak valid.", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR)
        );
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authSession.isLoggedIn()) {
            event.forwardTo(CashflowListView.class);
        }
    }
}