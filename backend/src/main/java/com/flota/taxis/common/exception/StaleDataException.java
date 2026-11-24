package com.flota.taxis.common.exception;

/** El registro fue modificado por otra persona desde que el cliente lo leyó (HTTP 409). */
public class StaleDataException extends RuntimeException {

    public StaleDataException(String message) {
        super(message);
    }
}
