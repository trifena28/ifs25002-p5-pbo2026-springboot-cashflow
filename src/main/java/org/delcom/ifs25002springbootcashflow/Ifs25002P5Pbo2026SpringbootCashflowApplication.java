package org.delcom.ifs25002springbootcashflow;

import org.delcom.ifs25002springbootcashflow.config.DotEnv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Ifs25002P5Pbo2026SpringbootCashflowApplication {

    public static void main(String[] args) {
        DotEnv.load();
        SpringApplication.run(Ifs25002P5Pbo2026SpringbootCashflowApplication.class, args);
    }
}