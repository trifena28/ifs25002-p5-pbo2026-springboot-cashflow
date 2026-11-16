package org.delcom.ifs25002springbootcashflow.adapter;

import org.delcom.ifs25002springbootcashflow.domain.Cashflow;
import org.delcom.ifs25002springbootcashflow.domain.CashflowRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryCashflowRepository implements CashflowRepository {
    private final Map<String, Cashflow> storage = Collections.synchronizedMap(new LinkedHashMap<>());

    @Override
    public Cashflow save(Cashflow cashflow) {
        if (cashflow.getId() == null || cashflow.getId().isBlank()) {
            cashflow.setId(UUID.randomUUID().toString());
        }
        storage.put(cashflow.getId(), cashflow);
        return cashflow;
    }

    @Override
    public List<Cashflow> findAll() {
        synchronized (storage) {
            return new ArrayList<>(storage.values());
        }
    }

    @Override
    public Optional<Cashflow> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public void deleteById(String id) {
        storage.remove(id);
    }
}