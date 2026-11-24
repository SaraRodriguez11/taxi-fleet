package com.flota.taxis.report;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Component;

import com.flota.taxis.daily.DailyTotalsRow;
import com.flota.taxis.daily.Totals;

/**
 * Lógica pura de agrupación (sin acceso a datos): recibe filas diarias y devuelve TODOS los
 * periodos del año (también los que no tienen registros, en cero) con desglose por taxi.
 * Al ser pura, se prueba con tests unitarios simples.
 */
@Component
public class ReportAggregator {

    public List<PeriodReport> aggregate(List<DailyTotalsRow> rows, ReportPeriod period, int year) {
        LocalDate yearStart = LocalDate.of(year, 1, 1);
        LocalDate yearEnd = LocalDate.of(year, 12, 31);
        Map<PeriodWindow, Map<Long, TaxiTotals>> byPeriod = new TreeMap<>();

        // 1. Todos los periodos del año, vacíos: así se ven también los meses/semanas sin movimiento
        for (LocalDate day = yearStart; !day.isAfter(yearEnd);) {
            PeriodWindow window = period.windowFor(day);
            byPeriod.computeIfAbsent(window, w -> new LinkedHashMap<>());
            day = window.end().plusDays(1);
        }

        // 2. Se reparten las filas diarias en su periodo
        for (DailyTotalsRow row : rows) {
            byPeriod.computeIfAbsent(period.windowFor(row.date()), w -> new LinkedHashMap<>())
                    .merge(row.taxiId(), new TaxiTotals(row.taxiId(), row.plate(), row.totals()), TaxiTotals::plus);
        }

        return byPeriod.entrySet().stream()
                .map(entry -> toReport(entry.getKey(), entry.getValue(), yearStart, yearEnd))
                .toList();
    }

    private PeriodReport toReport(PeriodWindow window, Map<Long, TaxiTotals> taxis, LocalDate yearStart,
            LocalDate yearEnd) {
        List<TaxiTotals> sorted = taxis.values().stream()
                .sorted(Comparator.comparing(TaxiTotals::plate))
                .toList();
        Totals total = sorted.stream().map(TaxiTotals::totals).reduce(Totals.ZERO, Totals::plus);
        // Una semana que cruza de año se recorta al año del reporte (los datos solo cubren ese año)
        LocalDate start = window.start().isBefore(yearStart) ? yearStart : window.start();
        LocalDate end = window.end().isAfter(yearEnd) ? yearEnd : window.end();
        return new PeriodReport(window.label(), start, end, sorted, total);
    }
}
