package org.delcom.ifs25002springbootcashflow.framework.view;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import org.delcom.ifs25002springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25002springbootcashflow.domain.entity.User;
import org.delcom.ifs25002springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25002springbootcashflow.usecase.UserUseCase;

import java.io.ByteArrayInputStream;
import java.util.concurrent.atomic.AtomicReference;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profil Akun - Aplikasi Cashflow")
public class ProfileView extends VerticalLayout implements BeforeEnterObserver {

    private final UserUseCase userUseCase;
    private final AuthSession authSession;
    private final FileStorage fileStorage;

    public ProfileView(UserUseCase userUseCase, AuthSession authSession, FileStorage fileStorage) {
        this.userUseCase = userUseCase;
        this.authSession = authSession;
        this.fileStorage = fileStorage;

        setSizeFull();
        setPadding(true);
        setAlignItems(FlexComponent.Alignment.CENTER);
    }

    private void renderContent() {
        removeAll();
        User sessionUser = authSession.getCurrentUser().orElse(null);
        if (sessionUser == null) {
            return;
        }

        User account = userUseCase.findById(sessionUser.getId());

        H2 sectionTitle = new H2("Pengaturan Profil");

        Avatar userAvatar = new Avatar(account.getName());
        userAvatar.setWidth("96px");
        userAvatar.setHeight("96px");
        if (account.getPhotoPath() != null && !account.getPhotoPath().isBlank()) {
            StreamResource imageRes = new StreamResource(
                    account.getPhotoPath(),
                    () -> new ByteArrayInputStream(fileStorage.loadAsBytes(account.getPhotoPath()))
            );
            userAvatar.setImageResource(imageRes);
        }

        TextField nameInput = new TextField("Nama Lengkap");
        nameInput.setValue(account.getName());
        nameInput.setWidth("350px");

        EmailField emailInput = new EmailField("Alamat Email");
        emailInput.setValue(account.getEmail());
        emailInput.setReadOnly(true);
        emailInput.setWidth("350px");

        AtomicReference<String> pendingPhoto = new AtomicReference<>(account.getPhotoPath());

        MemoryBuffer uploadBuffer = new MemoryBuffer();
        Upload photoUploader = new Upload(uploadBuffer);
        photoUploader.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp");
        photoUploader.addSucceededListener(event -> {
            String storedPath = fileStorage.store(uploadBuffer.getInputStream(), event.getFileName());
            pendingPhoto.set(storedPath);
            Notification.show("Foto profil siap disimpan.", 2000, Notification.Position.BOTTOM_START);
        });

        Button saveProfileBtn = new Button("Simpan Profil", event -> {
            try {
                User updatedAccount = userUseCase.updateProfile(account.getId(), nameInput.getValue(), pendingPhoto.get());
                authSession.login(updatedAccount);
                Notification.show("Perubahan profil tersimpan.", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                renderContent();
            } catch (Exception ex) {
                Notification.show(ex.getMessage(), 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveProfileBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveProfileBtn.setWidth("350px");

        H3 securityTitle = new H3("Ganti Kata Sandi");
        PasswordField currentPassInput = new PasswordField("Kata Sandi Saat Ini");
        currentPassInput.setWidth("350px");
        PasswordField newPassInput = new PasswordField("Kata Sandi Baru (minimal 6 karakter)");
        newPassInput.setWidth("350px");

        Button updatePasswordBtn = new Button("Perbarui Kata Sandi", event -> {
            try {
                userUseCase.changePassword(account.getId(), currentPassInput.getValue(), newPassInput.getValue());
                Notification.show("Kata sandi berhasil diperbarui.", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                currentPassInput.clear();
                newPassInput.clear();
            } catch (Exception ex) {
                Notification.show(ex.getMessage(), 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        updatePasswordBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        updatePasswordBtn.setWidth("350px");

        VerticalLayout contentLayout = new VerticalLayout(
                sectionTitle, userAvatar, photoUploader, nameInput, emailInput, saveProfileBtn,
                securityTitle, currentPassInput, newPassInput, updatePasswordBtn
        );
        contentLayout.setWidth("410px");
        contentLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        add(contentLayout);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!authSession.isLoggedIn()) {
            event.forwardTo(LoginView.class);
        } else {
            renderContent();
        }
    }
}