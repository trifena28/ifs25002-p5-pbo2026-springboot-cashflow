package org.delcom.ifs25002springbootcashflow.framework.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import org.delcom.ifs25002springbootcashflow.framework.auth.AuthSession;

public class MainLayout extends AppLayout {

    private final AuthSession authSession;

    public MainLayout(AuthSession authSession) {
        this.authSession = authSession;
        setupNavbar();
        setupSidebar();
    }

    private void setupNavbar() {
        H1 appTitle = new H1("Cashflow Tracker");
        appTitle.getStyle()
                .set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0");

        Span accountInfo = new Span();
        authSession.getCurrentUser().ifPresent(user ->
                accountInfo.setText(user.getName() + " [" + user.getRole() + "]"));

        Button signOutButton = new Button("Keluar", event -> {
            authSession.logout();
            getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        });
        signOutButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

        DrawerToggle drawerToggle = new DrawerToggle();
        drawerToggle.getElement().setAttribute("aria-label", "Buka menu navigasi");

        HorizontalLayout navbar = new HorizontalLayout(drawerToggle, appTitle, accountInfo, signOutButton);
        navbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        navbar.expand(appTitle);
        navbar.setWidthFull();
        navbar.addClassNames("py-0", "px-m");

        addToNavbar(navbar);
    }

    private void setupSidebar() {
        VerticalLayout menuList = new VerticalLayout();
        menuList.add(new RouterLink("Catatan Cashflow", CashflowListView.class));
        menuList.add(new RouterLink("Profil Akun", ProfileView.class));

        if (authSession.isAdmin()) {
            menuList.add(new RouterLink("Manajemen Pengguna", UserManagementView.class));
        }

        addToDrawer(menuList);
    }
}