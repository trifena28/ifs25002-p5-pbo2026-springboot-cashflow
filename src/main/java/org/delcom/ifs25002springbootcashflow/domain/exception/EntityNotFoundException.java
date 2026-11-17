package org.delcom.ifs25002springbootcashflow.domain.exception;

public class EntityNotFoundException extends DomainException {

    public EntityNotFoundException(String entityName, Object id) {
        super("Data " + entityName + " (ID: " + id + ") tidak ditemukan.");
    }
}