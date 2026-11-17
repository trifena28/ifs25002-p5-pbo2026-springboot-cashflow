package org.delcom.ifs25002springbootcashflow;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Meta;
import org.delcom.ifs25002springbootcashflow.config.DotEnv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Meta(name = "description", content = "Aplikasi Pencatatan Cashflow Pemasukan dan Pengeluaran Berbasis Spring Boot dan Vaadin")
public class Ifs25002P5Pbo2026SpringbootCashflowApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        DotEnv.load();
        SpringApplication.run(Ifs25002P5Pbo2026SpringbootCashflowApplication.class, args);
    }
}