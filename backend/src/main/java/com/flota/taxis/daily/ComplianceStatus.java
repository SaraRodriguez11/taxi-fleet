package com.flota.taxis.daily;

public enum ComplianceStatus {
    /** Aún no se ha recibido la producción del día. */
    PENDIENTE,
    /** Se recibió al menos la producción esperada. */
    CUMPLIDO,
    /** Se recibió producción, pero menos de la esperada. */
    PARCIAL,
    /** El taxi no trabajó (p. ej. Pico y Placa) y no se esperaba producción. */
    DESCANSO
}
