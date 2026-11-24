package com.flota.taxis.common.exception;

/** Violación de una regla de negocio (HTTP 422). */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
