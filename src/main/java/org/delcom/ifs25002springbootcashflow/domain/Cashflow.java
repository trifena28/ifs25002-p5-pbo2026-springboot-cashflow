package org.delcom.ifs25002springbootcashflow.domain;

import java.time.LocalDate;
import java.util.UUID;

public class Cashflow {
    private String id;
    private String tipe;
    private String kategori;
    private String deskripsi;
    private double nominal;
    private LocalDate tanggal;

    public Cashflow() {
        this.id = UUID.randomUUID().toString();
        this.tanggal = LocalDate.now();
    }

    public Cashflow(String tipe, String kategori, String deskripsi, double nominal, LocalDate tanggal) {
        this.id = UUID.randomUUID().toString();
        this.tipe = tipe;
        this.kategori = kategori;
        this.deskripsi = deskripsi;
        this.nominal = nominal;
        this.tanggal = tanggal != null ? tanggal : LocalDate.now();
    }

    public Cashflow(String id, String tipe, String kategori, String deskripsi, double nominal, LocalDate tanggal) {
        this.id = id;
        this.tipe = tipe;
        this.kategori = kategori;
        this.deskripsi = deskripsi;
        this.nominal = nominal;
        this.tanggal = tanggal != null ? tanggal : LocalDate.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTipe() {
        return tipe;
    }

    public void setTipe(String tipe) {
        this.tipe = tipe;
    }

    public String getKategori() {
        return kategori;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public double getNominal() {
        return nominal;
    }

    public void setNominal(double nominal) {
        this.nominal = nominal;
    }

    public LocalDate getTanggal() {
        return tanggal;
    }

    public void setTanggal(LocalDate tanggal) {
        this.tanggal = tanggal;
    }
}