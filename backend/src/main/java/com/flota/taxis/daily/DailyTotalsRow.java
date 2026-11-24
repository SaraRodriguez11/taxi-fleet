package com.flota.taxis.daily;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Proyección ligera para reportes: evita cargar entidades completas (y N+1). */
public record DailyTotalsRow(Long taxiId, String plate, LocalDate date, BigDecimal expected, BigDecimal received,
        BigDecimal savings, BigDecimal expenses, BigDecimal net) {

    public Totals totals() {
        return new Totals(expected, received, savings, expenses, net);
    }
}
