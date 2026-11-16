package org.delcom.ifs25002springbootcashflow.usecase;

import org.delcom.ifs25002springbootcashflow.domain.Cashflow;
import org.delcom.ifs25002springbootcashflow.domain.CashflowRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CashflowUseCase {
    private final CashflowRepository repository;

    public CashflowUseCase(CashflowRepository repository) {
        this.repository = repository;
    }

    public Cashflow simpanCashflow(Cashflow cashflow) {
        if (cashflow.getTipe() == null || cashflow.getTipe().isBlank()) {
            throw new IllegalArgumentException("Tipe transaksi wajib dipilih.");
        }
        if (cashflow.getKategori() == null || cashflow.getKategori().isBlank()) {
            throw new IllegalArgumentException("Kategori wajib diisi.");
        }
        if (cashflow.getDeskripsi() == null || cashflow.getDeskripsi().isBlank()) {
            throw new IllegalArgumentException("Deskripsi wajib diisi.");
        }
        if (cashflow.getNominal() <= 0) {
            throw new IllegalArgumentException("Nominal harus lebih besar dari 0.");
        }
        if (cashflow.getTanggal() == null) {
            cashflow.setTanggal(LocalDate.now());
        }
        return repository.save(cashflow);
    }

    public List<Cashflow> ambilSemuaCashflow() {
        return repository.findAll();
    }

    public List<Cashflow> cariCashflow(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return repository.findAll();
        }
        String lower = keyword.toLowerCase(Locale.ROOT).trim();
        return repository.findAll().stream()
                .filter(c -> (c.getDeskripsi() != null && c.getDeskripsi().toLowerCase(Locale.ROOT).contains(lower))
                        || (c.getKategori() != null && c.getKategori().toLowerCase(Locale.ROOT).contains(lower))
                        || (c.getTipe() != null && c.getTipe().toLowerCase(Locale.ROOT).contains(lower)))
                .collect(Collectors.toList());
    }

    public void hapusCashflow(String id) {
        repository.deleteById(id);
    }

    public double hitungTotalPemasukan() {
        return repository.findAll().stream()
                .filter(c -> "Pemasukan".equalsIgnoreCase(c.getTipe()))
                .mapToDouble(Cashflow::getNominal)
                .sum();
    }

    public double hitungTotalPengeluaran() {
        return repository.findAll().stream()
                .filter(c -> "Pengeluaran".equalsIgnoreCase(c.getTipe()))
                .mapToDouble(Cashflow::getNominal)
                .sum();
    }

    public double hitungSaldo() {
        return hitungTotalPemasukan() - hitungTotalPengeluaran();
    }
}