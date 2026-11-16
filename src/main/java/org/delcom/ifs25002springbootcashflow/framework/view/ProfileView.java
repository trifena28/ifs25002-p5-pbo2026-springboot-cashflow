package org.delcom.ifs25002springbootcashflow.framework.view;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25002springbootcashflow.domain.User;
import org.delcom.ifs25002springbootcashflow.framework.security.SessionManager;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profil Pengguna - Cashflow 11S25002")
public class ProfileView extends VerticalLayout {

    public ProfileView() {
        setPadding(true);
        setSpacing(true);

        User currentUser = SessionManager.getCurrentUser();
        String nama = currentUser != null ? currentUser.getNama() : "Admin Delcom";
        String username = currentUser != null ? currentUser.getUsername() : "admin@delcom.org";

        H2 heading = new H2("Profil Pengguna");
        Paragraph namaText = new Paragraph("Nama: " + nama);
        Paragraph emailText = new Paragraph("Email / Username: " + username);
        Paragraph nimText = new Paragraph("NIM Mahasiswa: 11S25002");

        add(heading, namaText, emailText, nimText);
    }
}