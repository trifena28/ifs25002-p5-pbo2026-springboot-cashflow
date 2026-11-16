package org.delcom.ifs25002springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25002springbootcashflow.domain.Cashflow;
import org.delcom.ifs25002springbootcashflow.usecase.CashflowUseCase;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

@Route("")
@PageTitle("Cashflow - 11S25002")
public class MainView extends VerticalLayout {

    private final CashflowUseCase cashflowUseCase;

    private final Span pemasukanValue = new Span();
    private final Span pengeluaranValue = new Span();
    private final Span saldoValue = new Span();

    private final ComboBox<String> tipeField = new ComboBox<>("Tipe");
    private final ComboBox<String> kategoriField = new ComboBox<>("Kategori");
    private final TextField deskripsiField = new TextField("Deskripsi");
    private final NumberField nominalField = new NumberField("Nominal (Rp)");
    private final DatePicker tanggalField = new DatePicker("Tanggal");

    private final TextField searchField = new TextField();
    private final Grid<Cashflow> grid = new Grid<>(Cashflow.class, false);

    private String editingId = null;

    public MainView(CashflowUseCase cashflowUseCase) {
        this.cashflowUseCase = cashflowUseCase;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Aplikasi Pencatatan Cashflow (11S25002)");

        HorizontalLayout summaryLayout = createSummaryCards();
        HorizontalLayout formLayout = createFormLayout();

        searchField.setPlaceholder("Cari deskripsi, kategori, atau tipe...");
        searchField.setWidth("320px");
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> refreshGrid());

        configureGrid();

        add(title, summaryLayout, formLayout, searchField, grid);
        refreshData();
    }

    private HorizontalLayout createSummaryCards() {
        VerticalLayout cardPemasukan = buildCard("Total Pemasukan", pemasukanValue, "#16a34a");
        VerticalLayout cardPengeluaran = buildCard("Total Pengeluaran", pengeluaranValue, "#dc2626");
        VerticalLayout cardSaldo = buildCard("Saldo Akhir", saldoValue, "#2563eb");

        HorizontalLayout layout = new HorizontalLayout(cardPemasukan, cardPengeluaran, cardSaldo);
        layout.setWidthFull();
        return layout;
    }

    private VerticalLayout buildCard(String labelText, Span valueSpan, String accentColor) {
        Span label = new Span(labelText);
        label.getStyle().set("font-size", "0.875rem").set("color", "#4b5563");

        valueSpan.getStyle()
                .set("font-size", "1.35rem")
                .set("font-weight", "bold")
                .set("color", accentColor);

        VerticalLayout card = new VerticalLayout(label, valueSpan);
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle()
                .set("border", "1px solid #e5e7eb")
                .set("border-radius", "8px")
                .set("background", "#ffffff");
        return card;
    }

    private HorizontalLayout createFormLayout() {
        tipeField.setItems("Pemasukan", "Pengeluaran");
        tipeField.setValue("Pengeluaran");

        kategoriField.setItems("Uang Saku", "Gaji", "Makanan & Minuman", "Transportasi", "Pendidikan", "Lainnya");
        kategoriField.setAllowCustomValue(true);
        kategoriField.addCustomValueSetListener(e -> kategoriField.setValue(e.getDetail()));

        deskripsiField.setPlaceholder("Contoh: Beli buku PBO");
        nominalField.setMin(1);
        nominalField.setPlaceholder("50000");
        tanggalField.setValue(LocalDate.now());

        Button simpanButton = new Button("Simpan", e -> handleSave());
        simpanButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button resetButton = new Button("Reset", e -> clearForm());

        HorizontalLayout form = new HorizontalLayout(
                tipeField, kategoriField, deskripsiField, nominalField, tanggalField, simpanButton, resetButton
        );
        form.setDefaultVerticalComponentAlignment(Alignment.END);
        form.setWidthFull();
        return form;
    }

    private void configureGrid() {
        grid.addColumn(Cashflow::getTanggal).setHeader("Tanggal").setSortable(true).setAutoWidth(true);
        grid.addColumn(Cashflow::getTipe).setHeader("Tipe").setSortable(true).setAutoWidth(true);
        grid.addColumn(Cashflow::getKategori).setHeader("Kategori").setSortable(true).setAutoWidth(true);
        grid.addColumn(Cashflow::getDeskripsi).setHeader("Deskripsi").setFlexGrow(1);
        grid.addColumn(c -> formatRupiah(c.getNominal())).setHeader("Nominal").setAutoWidth(true);

        grid.addComponentColumn(cashflow -> {
            Button editBtn = new Button("Edit", e -> populateForm(cashflow));
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);

            Button deleteBtn = new Button("Hapus", e -> {
                cashflowUseCase.hapusCashflow(cashflow.getId());
                if (cashflow.getId().equals(editingId)) {
                    clearForm();
                }
                refreshData();
                showNotification("Transaksi berhasil dihapus.", NotificationVariant.LUMO_CONTRAST);
            });
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);

            return new HorizontalLayout(editBtn, deleteBtn);
        }).setHeader("Aksi").setAutoWidth(true);

        grid.setSizeFull();
    }

    private void handleSave() {
        try {
            double nominal = nominalField.getValue() != null ? nominalField.getValue() : 0;
            Cashflow cashflow = new Cashflow(
                    editingId,
                    tipeField.getValue(),
                    kategoriField.getValue(),
                    deskripsiField.getValue(),
                    nominal,
                    tanggalField.getValue()
            );
            cashflowUseCase.simpanCashflow(cashflow);
            clearForm();
            refreshData();
            showNotification("Transaksi berhasil disimpan.", NotificationVariant.LUMO_SUCCESS);
        } catch (IllegalArgumentException ex) {
            showNotification(ex.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void populateForm(Cashflow cashflow) {
        editingId = cashflow.getId();
        tipeField.setValue(cashflow.getTipe());
        kategoriField.setValue(cashflow.getKategori());
        deskripsiField.setValue(cashflow.getDeskripsi());
        nominalField.setValue(cashflow.getNominal());
        tanggalField.setValue(cashflow.getTanggal());
    }

    private void clearForm() {
        editingId = null;
        tipeField.setValue("Pengeluaran");
        kategoriField.clear();
        deskripsiField.clear();
        nominalField.clear();
        tanggalField.setValue(LocalDate.now());
    }

    private void refreshData() {
        pemasukanValue.setText(formatRupiah(cashflowUseCase.hitungTotalPemasukan()));
        pengeluaranValue.setText(formatRupiah(cashflowUseCase.hitungTotalPengeluaran()));
        saldoValue.setText(formatRupiah(cashflowUseCase.hitungSaldo()));
        refreshGrid();
    }

    private void refreshGrid() {
        grid.setItems(cashflowUseCase.cariCashflow(searchField.getValue()));
    }

    private String formatRupiah(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.of("id", "ID"));
        return formatter.format(amount);
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_END);
        notification.addThemeVariants(variant);
    }
}