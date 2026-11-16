package org.delcom.ifs25002springbootcashflow.framework.view;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25002springbootcashflow.usecase.CashflowUseCase;

import java.text.NumberFormat;
import java.util.Locale;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard - Cashflow 11S25002")
public class DashboardView extends VerticalLayout {

    public DashboardView(CashflowUseCase cashflowUseCase) {
        setPadding(true);
        setSpacing(true);

        H2 heading = new H2("Dashboard Ringkasan Keuangan");
        Paragraph desc = new Paragraph("Pantau total pemasukan, pengeluaran, dan saldo akhir Anda.");

        HorizontalLayout cards = new HorizontalLayout(
                createCard("Total Pemasukan", formatRupiah(cashflowUseCase.hitungTotalPemasukan()), "#16a34a"),
                createCard("Total Pengeluaran", formatRupiah(cashflowUseCase.hitungTotalPengeluaran()), "#dc2626"),
                createCard("Saldo Akhir", formatRupiah(cashflowUseCase.hitungSaldo()), "#2563eb")
        );
        cards.setWidthFull();

        add(heading, desc, cards);
    }

    private VerticalLayout createCard(String label, String value, String color) {
        Span titleSpan = new Span(label);
        titleSpan.getStyle().set("font-size", "0.9rem").set("color", "#4b5563");

        Span valSpan = new Span(value);
        valSpan.getStyle().set("font-size", "1.4rem").set("font-weight", "bold").set("color", color);

        VerticalLayout card = new VerticalLayout(titleSpan, valSpan);
        card.setPadding(true);
        card.getStyle()
                .set("border", "1px solid #e5e7eb")
                .set("border-radius", "8px")
                .set("background", "#ffffff");
        return card;
    }

    private String formatRupiah(double amount) {
        return NumberFormat.getCurrencyInstance(Locale.of("id", "ID")).format(amount);
    }
}