package org.delcom.ifs25002springbootcashflow.framework;

import org.delcom.ifs25002springbootcashflow.adapter.InMemoryCashflowRepository;
import org.delcom.ifs25002springbootcashflow.adapter.InMemoryUserRepository;
import org.delcom.ifs25002springbootcashflow.domain.Cashflow;
import org.delcom.ifs25002springbootcashflow.domain.CashflowRepository;
import org.delcom.ifs25002springbootcashflow.domain.User;
import org.delcom.ifs25002springbootcashflow.domain.UserRepository;
import org.delcom.ifs25002springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25002springbootcashflow.usecase.CashflowUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class BeanConfig {

    @Bean
    public UserRepository userRepository() {
        InMemoryUserRepository repo = new InMemoryUserRepository();
        repo.save(new User("Admin Delcom", "admin@delcom.org", "admin123"));
        return repo;
    }

    @Bean
    public AuthUseCase authUseCase(UserRepository userRepository) {
        return new AuthUseCase(userRepository);
    }

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