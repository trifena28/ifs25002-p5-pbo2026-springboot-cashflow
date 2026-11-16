package org.delcom.ifs25002springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLink;
import org.delcom.ifs25002springbootcashflow.framework.security.SessionManager;

public class MainLayout extends AppLayout implements BeforeEnterObserver {

    public MainLayout() {
        H3 logo = new H3("Cashflow 11S25002");
        logo.getStyle().set("margin", "0");

        RouterLink dashboardLink = new RouterLink("Dashboard", DashboardView.class);
        RouterLink cashflowLink = new RouterLink("Kelola Cashflow", MainView.class);
        RouterLink profileLink = new RouterLink("Profil", ProfileView.class);

        Button logoutBtn = new Button("Keluar", e -> {
            SessionManager.logout();
            UI.getCurrent().navigate("login");
        });
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);

        HorizontalLayout nav = new HorizontalLayout(logo, dashboardLink, cashflowLink, profileLink, logoutBtn);
        nav.setWidthFull();
        nav.setPadding(true);
        nav.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        nav.expand(logo);

        addToNavbar(nav);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!SessionManager.isLoggedIn()) {
            event.forwardTo(LoginView.class);
        }
    }
}