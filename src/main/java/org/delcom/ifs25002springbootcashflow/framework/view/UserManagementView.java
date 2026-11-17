package org.delcom.ifs25002springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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

@Route(value = "admin/users", layout = MainLayout.class)
@PageTitle("Manajemen Pengguna - Admin")
public class UserManagementView extends VerticalLayout implements BeforeEnterObserver {

    private final UserUseCase userUseCase;
    private final AuthSession authSession;
    private final FileStorage fileStorage;
    private final Grid<User> userGrid = new Grid<>(User.class, false);

    public UserManagementView(UserUseCase userUseCase, AuthSession authSession, FileStorage fileStorage) {
        this.userUseCase = userUseCase;
        this.authSession = authSession;
        this.fileStorage = fileStorage;

        setSizeFull();
        setPadding(true);

        H2 pageHeading = new H2("Daftar Pengguna Terdaftar");
        configureUserGrid();
        add(pageHeading, userGrid);
    }

    private void configureUserGrid() {
        userGrid.setSizeFull();

        userGrid.addComponentColumn(account -> {
            Avatar avatar = new Avatar(account.getName());
            if (account.getPhotoPath() != null && !account.getPhotoPath().isBlank()) {
                StreamResource imageRes = new StreamResource(
                        account.getPhotoPath(),
                        () -> new ByteArrayInputStream(fileStorage.loadAsBytes(account.getPhotoPath()))
                );
                avatar.setImageResource(imageRes);
            }
            return avatar;
        }).setHeader("Avatar").setWidth("75px").setFlexGrow(0);

        userGrid.addColumn(User::getId).setHeader("ID").setWidth("65px").setFlexGrow(0);
        userGrid.addColumn(User::getName).setHeader("Nama Pengguna").setSortable(true);
        userGrid.addColumn(User::getEmail).setHeader("Email").setSortable(true);
        userGrid.addColumn(account -> account.getRole().name()).setHeader("Peran");

        userGrid.addComponentColumn(account -> {
            Button inspectBtn = new Button("Lihat Transaksi", event ->
                    UI.getCurrent().navigate(UserTransactionReadOnlyView.class, account.getId().toString()));
            inspectBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            return inspectBtn;
        }).setHeader("Aksi");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!authSession.isLoggedIn() || !authSession.isAdmin()) {
            event.forwardTo(CashflowListView.class);
        } else {
            userGrid.setItems(userUseCase.findAll());
        }
    }
}