package org.delcom.ifs25002springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import org.delcom.ifs25002springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25002springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25002springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25002springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25002springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25002springbootcashflow.framework.component.FinancePresenter;
import org.delcom.ifs25002springbootcashflow.framework.component.ImageZoomDialog;
import org.delcom.ifs25002springbootcashflow.usecase.TransactionUseCase;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Catatan Cashflow")
public class CashflowListView extends VerticalLayout implements BeforeEnterObserver {

    private final TransactionUseCase transactionUseCase;
    private final AuthSession authSession;
    private final FileStorage fileStorage;

    private final H3 incomeValue = new H3("Rp0");
    private final H3 expenseValue = new H3("Rp0");
    private final H3 balanceValue = new H3("Rp0");

    private final TextField keywordInput = new TextField();
    private final ComboBox<TransactionType> typeFilter = new ComboBox<>();
    private final ComboBox<SortOption> sortSelector = new ComboBox<>();
    private final Grid<Transaction> transactionGrid = new Grid<>(Transaction.class, false);

    public CashflowListView(TransactionUseCase transactionUseCase, AuthSession authSession, FileStorage fileStorage) {
        this.transactionUseCase = transactionUseCase;
        this.authSession = authSession;
        this.fileStorage = fileStorage;

        setSizeFull();
        setPadding(true);

        H2 heading = new H2("Ringkasan & Catatan Cashflow");
        HorizontalLayout summaryRow = buildSummaryCards();
        HorizontalLayout controlBar = buildControlBar();
        setupGrid();

        add(heading, summaryRow, controlBar, transactionGrid);
    }

    private HorizontalLayout buildSummaryCards() {
        Div incomeCard = createMetricCard("Total Pemasukan", incomeValue, "#16a34a");
        Div expenseCard = createMetricCard("Total Pengeluaran", expenseValue, "#dc2626");
        Div balanceCard = createMetricCard("Saldo Akhir", balanceValue, "#2563eb");

        HorizontalLayout row = new HorizontalLayout(incomeCard, expenseCard, balanceCard);
        row.setWidthFull();
        return row;
    }

    private Div createMetricCard(String title, H3 valueHeading, String accentColor) {
        Span label = new Span(title);
        label.getStyle().set("font-size", "var(--lumo-font-size-s)").set("color", "var(--lumo-secondary-text-color)");
        valueHeading.getStyle().set("margin", "4px 0 0 0").set("color", accentColor);

        Div card = new Div(label, valueHeading);
        card.getStyle()
                .set("padding", "14px 18px")
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "10px")
                .set("flex", "1");
        return card;
    }

    private HorizontalLayout buildControlBar() {
        keywordInput.setPlaceholder("Cari judul atau catatan...");
        keywordInput.setClearButtonVisible(true);
        keywordInput.setWidth("260px");
        keywordInput.addValueChangeListener(e -> reloadData());

        typeFilter.setPlaceholder("Semua Tipe");
        typeFilter.setItems(TransactionType.values());
        typeFilter.setItemLabelGenerator(TransactionType::getLabel);
        typeFilter.setClearButtonVisible(true);
        typeFilter.setWidth("180px");
        typeFilter.addValueChangeListener(e -> reloadData());

        sortSelector.setItems(SortOption.values());
        sortSelector.setItemLabelGenerator(SortOption::getLabel);
        sortSelector.setValue(SortOption.NEWEST);
        sortSelector.setWidth("190px");
        sortSelector.addValueChangeListener(e -> reloadData());

        Button newTxButton = new Button("Tambah Transaksi", e -> openFormDialog(null));
        newTxButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout bar = new HorizontalLayout(keywordInput, typeFilter, sortSelector, newTxButton);
        bar.setWidthFull();
        bar.setAlignItems(FlexComponent.Alignment.BASELINE);
        return bar;
    }

    private void setupGrid() {
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
        transactionGrid.addColumn(Transaction::getTitle).setHeader("Judul").setSortable(true);
        transactionGrid.addColumn(Transaction::getDescription).setHeader("Deskripsi");
        transactionGrid.addColumn(tx -> FinancePresenter.formatCurrency(tx.getAmount())).setHeader("Nominal").setSortable(true);

        transactionGrid.addComponentColumn(tx -> {
            Button editBtn = new Button("Ubah", e -> openFormDialog(tx));
            editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

            Button removeBtn = new Button("Hapus", e -> {
                Long actorId = authSession.getCurrentUser().orElseThrow().getId();
                transactionUseCase.delete(tx.getId(), actorId);
                reloadData();
                Notification.show("Transaksi dihapus.", 2500, Notification.Position.BOTTOM_START);
            });
            removeBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

            return new HorizontalLayout(editBtn, removeBtn);
        }).setHeader("Aksi");
    }

    private void reloadData() {
        if (!authSession.isLoggedIn()) {
            return;
        }
        Long currentUserId = authSession.getCurrentUser().orElseThrow().getId();
        List<Transaction> items = transactionUseCase.search(
                currentUserId,
                keywordInput.getValue(),
                typeFilter.getValue(),
                sortSelector.getValue()
        );
        transactionGrid.setItems(items);

        incomeValue.setText(FinancePresenter.formatCurrency(transactionUseCase.getTotalIncome(currentUserId)));
        expenseValue.setText(FinancePresenter.formatCurrency(transactionUseCase.getTotalExpense(currentUserId)));
        balanceValue.setText(FinancePresenter.formatCurrency(transactionUseCase.getBalance(currentUserId)));
    }

    private void openFormDialog(Transaction existing) {
        Dialog modal = new Dialog();
        modal.setHeaderTitle(existing == null ? "Catat Transaksi Baru" : "Ubah Data Transaksi");

        ComboBox<TransactionType> typeBox = new ComboBox<>("Tipe Transaksi");
        typeBox.setItems(TransactionType.values());
        typeBox.setItemLabelGenerator(TransactionType::getLabel);
        typeBox.setWidthFull();
        typeBox.setRequired(true);

        TextField titleBox = new TextField("Judul Transaksi");
        titleBox.setWidthFull();
        titleBox.setRequired(true);

        NumberField amountBox = new NumberField("Nominal (Rp)");
        amountBox.setWidthFull();
        amountBox.setMin(1);
        amountBox.setRequiredIndicatorVisible(true);

        TextArea descBox = new TextArea("Catatan / Deskripsi");
        descBox.setWidthFull();

        AtomicReference<String> storedAttachment = new AtomicReference<>(existing != null ? existing.getAttachmentPath() : null);

        MemoryBuffer buffer = new MemoryBuffer();
        Upload uploader = new Upload(buffer);
        uploader.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp");
        uploader.addSucceededListener(event -> {
            String savedName = fileStorage.store(buffer.getInputStream(), event.getFileName());
            storedAttachment.set(savedName);
            Notification.show("Lampiran bukti berhasil diunggah.", 2000, Notification.Position.BOTTOM_START);
        });

        if (existing != null) {
            typeBox.setValue(existing.getType());
            titleBox.setValue(existing.getTitle());
            amountBox.setValue(existing.getAmount());
            if (existing.getDescription() != null) {
                descBox.setValue(existing.getDescription());
            }
        }

        VerticalLayout formLayout = new VerticalLayout(typeBox, titleBox, amountBox, descBox, uploader);
        formLayout.setWidth("420px");
        modal.add(formLayout);

        Button saveBtn = new Button("Simpan", event -> {
            try {
                Long actorId = authSession.getCurrentUser().orElseThrow().getId();
                if (existing == null) {
                    transactionUseCase.create(
                            actorId,
                            titleBox.getValue(),
                            descBox.getValue(),
                            typeBox.getValue(),
                            amountBox.getValue(),
                            storedAttachment.get()
                    );
                } else {
                    transactionUseCase.update(
                            existing.getId(),
                            actorId,
                            titleBox.getValue(),
                            descBox.getValue(),
                            typeBox.getValue(),
                            amountBox.getValue(),
                            storedAttachment.get()
                    );
                }
                reloadData();
                modal.close();
                Notification.show("Data transaksi tersimpan.", 2500, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show(ex.getMessage(), 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Batal", event -> modal.close());
        modal.getFooter().add(cancelBtn, saveBtn);
        modal.open();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!authSession.isLoggedIn()) {
            event.forwardTo(LoginView.class);
        } else {
            reloadData();
        }
    }
}