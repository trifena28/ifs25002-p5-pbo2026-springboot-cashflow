package org.delcom.ifs25002springbootcashflow.domain;

import java.util.List;
import java.util.Optional;

public interface CashflowRepository {
    Cashflow save(Cashflow cashflow);
    List<Cashflow> findAll();
    Optional<Cashflow> findById(String id);
    void deleteById(String id);
}