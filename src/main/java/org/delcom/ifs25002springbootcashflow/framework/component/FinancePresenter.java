package org.delcom.ifs25002springbootcashflow.framework.component;

import org.delcom.ifs25002springbootcashflow.domain.entity.TransactionType;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

public class FinancePresenter {

    private static final Locale INDONESIA_LOCALE = Locale.forLanguageTag("id-ID");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", INDONESIA_LOCALE);

    public static String formatCurrency(Double amount) {
        double value = Objects.requireNonNullElse(amount, 0.0);
        NumberFormat formatter = NumberFormat.getCurrencyInstance(INDONESIA_LOCALE);
        formatter.setMaximumFractionDigits(0);
        return formatter.format(value);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMAT) : "-";
    }

    public static String formatTypeLabel(TransactionType type) {
        return type != null ? type.getLabel() : "-";
    }
}