package org.delcom.ifs25002springbootcashflow.domain.entity;

public enum TransactionType {
    INCOME("Pemasukan"),
    EXPENSE("Pengeluaran");

    private final String textLabel;

    TransactionType(String textLabel) {
        this.textLabel = textLabel;
    }

    public String getLabel() {
        return this.textLabel;
    }
}