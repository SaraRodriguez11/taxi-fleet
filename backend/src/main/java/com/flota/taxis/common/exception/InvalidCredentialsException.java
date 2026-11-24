package com.flota.taxis.common.exception;

/** Correo o contraseña incorrectos (HTTP 401). El mensaje es el mismo para no revelar si el correo existe. */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Correo o contraseña incorrectos");
    }
}
