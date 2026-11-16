package org.delcom.ifs25002springbootcashflow.framework;

import org.delcom.ifs25002springbootcashflow.adapter.InMemoryCashflowRepository;
import org.delcom.ifs25002springbootcashflow.domain.Cashflow;
import org.delcom.ifs25002springbootcashflow.domain.CashflowRepository;
import org.delcom.ifs25002springbootcashflow.usecase.CashflowUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class BeanConfig {

    @Bean
    public CashflowRepository cashflowRepository() {
        InMemoryCashflowRepository repo = new InMemoryCashflowRepository();
        repo.save(new Cashflow("Pemasukan", "Uang Saku", "Uang saku bulanan", 1500000, LocalDate.now()));
        repo.save(new Cashflow("Pengeluaran", "Makanan & Minuman", "Makan siang kantin", 25000, LocalDate.now()));
        return repo;
    }

    @Bean
    public CashflowUseCase cashflowUseCase(CashflowRepository cashflowRepository) {
        return new CashflowUseCase(cashflowRepository);
    }
}