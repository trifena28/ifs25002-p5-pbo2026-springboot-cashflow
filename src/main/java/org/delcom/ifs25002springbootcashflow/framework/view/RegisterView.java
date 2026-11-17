package org.delcom.ifs25002springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.delcom.ifs25002springbootcashflow.domain.entity.Role;
import org.delcom.ifs25002springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25002springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25002springbootcashflow.usecase.AuthUseCase;

@Route("register")
@PageTitle("Registrasi - Aplikasi Cashflow")
public class RegisterView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthUseCase authUseCase;
    private final AuthSession authSession;

    public RegisterView(AuthUseCase authUseCase, AuthSession authSession) {
        this.authUseCase = authUseCase;
        this.authSession = authSession;

        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        H2 heading = new H2("Buat Akun Baru");

        TextField fullNameInput = new TextField("Nama Lengkap");
        fullNameInput.setWidth("330px");
        fullNameInput.setRequiredIndicatorVisible(true);

        EmailField emailInput = new EmailField("Alamat Email");
        emailInput.setWidth("330px");
        emailInput.setRequiredIndicatorVisible(true);

        PasswordField passwordInput = new PasswordField("Kata Sandi (minimal 6 karakter)");
        passwordInput.setWidth("330px");
        passwordInput.setRequiredIndicatorVisible(true);

        Button registerButton = new Button("Daftar Akun", event ->
                processRegistration(fullNameInput.getValue(), emailInput.getValue(), passwordInput.getValue()));
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.setWidth("330px");

        RouterLink loginLink = new RouterLink("Sudah terdaftar? Masuk di sini", LoginView.class);

        VerticalLayout formBox = new VerticalLayout(heading, fullNameInput, emailInput, passwordInput, registerButton, loginLink);
        formBox.setWidth("380px");
        formBox.setAlignItems(FlexComponent.Alignment.CENTER);
        add(formBox);
    }

    private void processRegistration(String name, String email, String password) {
        try {
            authUseCase.register(name, email, password, Role.USER);
            Notification.show("Akun berhasil dibuat. Silakan masuk.", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        } catch (ValidationException ex) {
            Notification.show(ex.getMessage(), 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authSession.isLoggedIn()) {
            event.forwardTo(CashflowListView.class);
        }
    }
}