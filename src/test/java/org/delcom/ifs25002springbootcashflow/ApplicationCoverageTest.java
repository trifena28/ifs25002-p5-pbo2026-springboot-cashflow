package org.delcom.ifs25002springbootcashflow;

import org.delcom.ifs25002springbootcashflow.adapter.InMemoryCashflowRepository;
import org.delcom.ifs25002springbootcashflow.adapter.InMemoryUserRepository;
import org.delcom.ifs25002springbootcashflow.domain.Cashflow;
import org.delcom.ifs25002springbootcashflow.domain.User;
import org.delcom.ifs25002springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25002springbootcashflow.usecase.CashflowUseCase;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationCoverageTest {

    @Test
    void testUserAndAuthUseCaseFullCoverage() {
        User u1 = new User();
        assertNotNull(u1.getId());
        u1.setId("u-1");
        u1.setNama("Trifena");
        u1.setUsername("trifena@delcom.org");
        u1.setPassword("secret");
        assertEquals("u-1", u1.getId());
        assertEquals("Trifena", u1.getNama());
        assertEquals("trifena@delcom.org", u1.getUsername());
        assertEquals("secret", u1.getPassword());

        InMemoryUserRepository userRepo = new InMemoryUserRepository();
        assertTrue(userRepo.findByUsername(null).isEmpty());

        AuthUseCase auth = new AuthUseCase(userRepo);
        assertThrows(IllegalArgumentException.class, () -> auth.register(null, "a@b.com", "1234"));
        assertThrows(IllegalArgumentException.class, () -> auth.register("   ", "a@b.com", "1234"));
        assertThrows(IllegalArgumentException.class, () -> auth.register("Nama", null, "1234"));
        assertThrows(IllegalArgumentException.class, () -> auth.register("Nama", "  ", "1234"));
        assertThrows(IllegalArgumentException.class, () -> auth.register("Nama", "a@b.com", null));
        assertThrows(IllegalArgumentException.class, () -> auth.register("Nama", "a@b.com", "123"));

        User registered = auth.register("Admin Delcom", "admin@delcom.org", "admin123");
        assertEquals("Admin Delcom", registered.getNama());
        assertThrows(IllegalArgumentException.class, () -> auth.register("Admin 2", "admin@delcom.org", "admin123"));

        assertTrue(auth.login(null, "admin123").isEmpty());
        assertTrue(auth.login("admin@delcom.org", null).isEmpty());
        assertTrue(auth.login("admin@delcom.org", "salah").isEmpty());
        assertTrue(auth.login("tidakada@delcom.org", "admin123").isEmpty());
        assertTrue(auth.login("admin@delcom.org", "admin123").isPresent());
    }

    @Test
    void testCashflowAndUseCaseFullCoverage() {
        LocalDate today = LocalDate.now();
        Cashflow c1 = new Cashflow();
        assertNotNull(c1.getId());
        assertNotNull(c1.getTanggal());
        c1.setId("c-1");
        c1.setTipe("Pemasukan");
        c1.setKategori("Gaji");
        c1.setDeskripsi("Gaji bulanan");
        c1.setNominal(1000000);
        c1.setTanggal(today);
        assertEquals("c-1", c1.getId());
        assertEquals("Pemasukan", c1.getTipe());
        assertEquals("Gaji", c1.getKategori());
        assertEquals("Gaji bulanan", c1.getDeskripsi());
        assertEquals(1000000, c1.getNominal());
        assertEquals(today, c1.getTanggal());

        Cashflow cNullDate = new Cashflow("Pengeluaran", "Makan", "Sarapan", 15000, null);
        assertNotNull(cNullDate.getTanggal());

        Cashflow cWithIdNullDate = new Cashflow("id-2", "Pengeluaran", "Makan", "Makan siang", 20000, null);
        assertEquals("id-2", cWithIdNullDate.getId());
        assertNotNull(cWithIdNullDate.getTanggal());

        Cashflow cWithIdNonNullDate = new Cashflow("id-3", "Pemasukan", "Gaji", "Bonus", 30000, today);
        assertEquals("id-3", cWithIdNonNullDate.getId());
        assertEquals(today, cWithIdNonNullDate.getTanggal());

        InMemoryCashflowRepository repo = new InMemoryCashflowRepository();
        Cashflow noId = new Cashflow(null, "Pemasukan", "Bonus", "Bonus proyek", 500000, today);
        repo.save(noId);
        assertNotNull(noId.getId());
        Cashflow blankId = new Cashflow("  ", "Pemasukan", "Bonus", "Bonus 2", 100000, today);
        repo.save(blankId);
        assertFalse(blankId.getId().isBlank());
        assertTrue(repo.findById(noId.getId()).isPresent());

        CashflowUseCase useCase = new CashflowUseCase(repo);

        assertThrows(IllegalArgumentException.class, () -> useCase.simpanCashflow(new Cashflow(null, "Kat", "Desk", 100, today)));
        assertThrows(IllegalArgumentException.class, () -> useCase.simpanCashflow(new Cashflow(" ", "Kat", "Desk", 100, today)));
        assertThrows(IllegalArgumentException.class, () -> useCase.simpanCashflow(new Cashflow("Pemasukan", null, "Desk", 100, today)));
        assertThrows(IllegalArgumentException.class, () -> useCase.simpanCashflow(new Cashflow("Pemasukan", " ", "Desk", 100, today)));
        assertThrows(IllegalArgumentException.class, () -> useCase.simpanCashflow(new Cashflow("Pemasukan", "Kat", null, 100, today)));
        assertThrows(IllegalArgumentException.class, () -> useCase.simpanCashflow(new Cashflow("Pemasukan", "Kat", " ", 100, today)));
        assertThrows(IllegalArgumentException.class, () -> useCase.simpanCashflow(new Cashflow("Pemasukan", "Kat", "Desk", 0, today)));

        Cashflow manualNullDate = new Cashflow();
        manualNullDate.setTipe("Pengeluaran");
        manualNullDate.setKategori("Transport");
        manualNullDate.setDeskripsi("Bensin");
        manualNullDate.setNominal(50000);
        manualNullDate.setTanggal(null);
        useCase.simpanCashflow(manualNullDate);
        assertNotNull(manualNullDate.getTanggal());

        assertEquals(3, useCase.ambilSemuaCashflow().size());
        assertEquals(3, useCase.cariCashflow(null).size());
        assertEquals(3, useCase.cariCashflow("   ").size());
        assertEquals(1, useCase.cariCashflow("bensin").size());
        assertEquals(1, useCase.cariCashflow("transport").size());
        assertEquals(1, useCase.cariCashflow("pengeluaran").size());

        Cashflow nullFields = new Cashflow();
        repo.save(nullFields);
        assertEquals(0, useCase.cariCashflow("zzzzz").size());
        useCase.hapusCashflow(nullFields.getId());

        assertEquals(600000, useCase.hitungTotalPemasukan());
        assertEquals(50000, useCase.hitungTotalPengeluaran());
        assertEquals(550000, useCase.hitungSaldo());
    }
}
