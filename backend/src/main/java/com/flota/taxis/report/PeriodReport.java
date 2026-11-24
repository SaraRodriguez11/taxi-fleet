package com.flota.taxis.report;

import java.time.LocalDate;
import java.util.List;

import com.flota.taxis.daily.Totals;

/** Un periodo (semana o mes) con el desglose por taxi y el total consolidado. */
public record PeriodReport(String label, LocalDate start, LocalDate end, List<TaxiTotals> taxis, Totals totals) {
}
