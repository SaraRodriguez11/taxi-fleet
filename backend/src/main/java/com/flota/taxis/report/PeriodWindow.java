package com.flota.taxis.report;

import java.time.LocalDate;

/** Ventana de un periodo de reporte. Se ordena cronológicamente por fecha de inicio. */
public record PeriodWindow(String label, LocalDate start, LocalDate end) implements Comparable<PeriodWindow> {

    @Override
    public int compareTo(PeriodWindow other) {
        return start.compareTo(other.start);
    }
}
