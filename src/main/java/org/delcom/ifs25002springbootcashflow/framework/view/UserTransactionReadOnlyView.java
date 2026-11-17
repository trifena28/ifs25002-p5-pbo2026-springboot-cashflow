package org.delcom.ifs25002springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.WildcardParameter;
import com.vaadin.flow.server.StreamResource;
import org.delcom.ifs25002springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25002springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25002springbootcashflow.domain.entity.User;
import org.delcom.ifs25002springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25002springbootcashflow.framework.component.FinancePresenter;
import org.delcom.ifs25002springbootcashflow.framework.component.ImageZoomDialog;
import org.delcom.ifs25002springbootcashflow.usecase.TransactionUseCase;
import org.delcom.ifs25002springbootcashflow.usecase.UserUseCase;

import java.io.ByteArrayInputStream;
import java.util.List;

@Route(value = "admin/users/:userId/transactions", layout = MainLayout.class)
@PageTitle("Transaksi Pengguna - Mode Baca")
public class UserTransactionReadOnlyView extends VerticalLayout implements BeforeEnterObserver, HasUrlParameter<String> {

    private final TransactionUseCase transactionUseCase;
    private final UserUseCase userUseCase;
    private final AuthSession authSession;
    private final FileStorage fileStorage;

    private Long inspectedUserId;
    private final H2 heading = new H2("Riwayat Transaksi Pengguna");
    private final Grid<Transaction> transactionGrid = new Grid<>(Transaction.class, false);

    public UserTransactionReadOnlyView(TransactionUseCase transactionUseCase, UserUseCase userUseCase,
                                       AuthSession authSession, FileStorage fileStorage) {
        this.transactionUseCase = transactionUseCase;
        this.userUseCase = userUseCase;
        this.authSession = authSession;
        this.fileStorage = fileStorage;

        setSizeFull();
        setPadding(true);

        Button backButton = new Button("Kembali ke Daftar Pengguna", e ->
                getUI().ifPresent(ui -> ui.navigate(UserManagementView.class)));

        configureGrid();
        add(new HorizontalLayout(backButton), heading, transactionGrid);
    }

    private void configureGrid() {
        transactionGrid.setSizeFull();

        transactionGrid.addComponentColumn(tx -> {
            if (tx.getAttachmentPath() != null && !tx.getAttachmentPath().isBlank()) {
                StreamResource res = new StreamResource(
                        tx.getAttachmentPath(),
                        () -> new ByteArrayInputStream(fileStorage.loadAsBytes(tx.getAttachmentPath()))
                );
                Image thumb = new Image(res, "Bukti");
                thumb.setWidth("44px");
                thumb.setHeight("44px");
                thumb.getStyle().set("object-fit", "cover").set("border-radius", "6px").set("cursor", "pointer");
                thumb.addClickListener(e -> new ImageZoomDialog(thumb.getSrc(), tx.getTitle()).open());
                return thumb;
            }
            return new Span("-");
        }).setHeader("Bukti").setWidth("80px").setFlexGrow(0);

        transactionGrid.addColumn(tx -> FinancePresenter.formatDateTime(tx.getCreatedAt())).setHeader("Waktu").setWidth("165px").setFlexGrow(0);
        transactionGrid.addColumn(tx -> FinancePresenter.formatTypeLabel(tx.getType())).setHeader("Tipe").setWidth("130px").setFlexGrow(0);
        transactionGrid.addColumn(Transaction::getTitle).setHeader("Judul");
        transactionGrid.addColumn(Transaction::getDescription).setHeader("Deskripsi");
        transactionGrid.addColumn(tx -> FinancePresenter.formatCurrency(tx.getAmount())).setHeader("Nominal");
    }

    @Override
    public void setParameter(BeforeEvent event, @WildcardParameter String parameter) {
        try {
            this.inspectedUserId = Long.parseLong(parameter);
        } catch (NumberFormatException ex) {
            this.inspectedUserId = null;
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!authSession.isLoggedIn() || !authSession.isAdmin()) {
            event.forwardTo(CashflowListView.class);
            return;
        }
        if (inspectedUserId == null) {
            event.forwardTo(UserManagementView.class);
            return;
        }
        try {
            User targetUser = userUseCase.findById(inspectedUserId);
            heading.setText("Transaksi Milik: " + targetUser.getName() + " (" + targetUser.getEmail() + ")");
            List<Transaction> records = transactionUseCase.findByUserId(inspectedUserId);
            transactionGrid.setItems(records);
        } catch (Exception ex) {
            event.forwardTo(UserManagementView.class);
        }
    }
}