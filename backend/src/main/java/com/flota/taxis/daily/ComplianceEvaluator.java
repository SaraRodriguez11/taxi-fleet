package com.flota.taxis.daily;

import java.math.BigDecimal;

/** Regla de negocio pura (sin dependencias de Spring): fácil de probar. */
public final class ComplianceEvaluator {

    private ComplianceEvaluator() {
    }

    public static ComplianceStatus evaluate(boolean picoPlaca, BigDecimal expected, BigDecimal received) {
        if (received.signum() > 0 && received.compareTo(expected) >= 0) {
            return ComplianceStatus.CUMPLIDO;
        }
        if (received.signum() > 0) {
            return ComplianceStatus.PARCIAL;
        }
        return picoPlaca ? ComplianceStatus.DESCANSO : ComplianceStatus.PENDIENTE;
    }
}
