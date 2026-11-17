package org.delcom.ifs25002springbootcashflow.domain.entity;

public enum SortOption {
    NEWEST("Terbaru"),
    OLDEST("Terlama"),
    TITLE_ASC("Judul A-Z"),
    TITLE_DESC("Judul Z-A"),
    AMOUNT_HIGH("Nominal Terbesar"),
    AMOUNT_LOW("Nominal Terkecil");

    private final String displayLabel;

    SortOption(String displayLabel) {
        this.displayLabel = displayLabel;
    }

    public String getLabel() {
        return this.displayLabel;
    }
}