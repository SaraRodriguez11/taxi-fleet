package com.flota.taxis.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.flota.taxis.daily.DailyTotalsRow;

class ReportAggregatorTest {

    private final ReportAggregator aggregator = new ReportAggregator();

    private static DailyTotalsRow row(long taxiId, String plate, String date, String received) {
        BigDecimal amount = new BigDecimal(received);
        // esperado 100, ahorro 10, gastos 5 => neto = recibido - 15
        return new DailyTotalsRow(taxiId, plate, LocalDate.parse(date), new BigDecimal("100"), amount,
                new BigDecimal("10"), new BigDecimal("5"), amount.subtract(new BigDecimal("15")));
    }

    @Test
    void elReporteMensualTraeLosDoceMesesDelAnioAunqueNoTenganDatos() {
        List<PeriodReport> report = aggregator.aggregate(List.of(row(1, "AAA111", "2026-03-05", "100")),
                ReportPeriod.MONTH, 2026);

        assertThat(report).hasSize(12);
        assertThat(report.get(0).label()).isEqualTo("Enero");
        assertThat(report.get(11).label()).isEqualTo("Diciembre");
        assertThat(report.get(0).taxis()).isEmpty();
        assertThat(report.get(0).totals().received()).isEqualByComparingTo("0");
        assertThat(report.get(2).totals().received()).isEqualByComparingTo("100");
    }

    @Test
    void agrupaPorMesYTaxiConTotalesConsolidados() {
        List<DailyTotalsRow> rows = List.of(
                row(1, "AAA111", "2026-01-05", "100"),
                row(1, "AAA111", "2026-01-20", "120"),
                row(2, "BBB222", "2026-01-21", "90"),
                row(1, "AAA111", "2026-02-02", "110"));

        List<PeriodReport> report = aggregator.aggregate(rows, ReportPeriod.MONTH, 2026);

        PeriodReport january = report.get(0);
        assertThat(january.taxis()).hasSize(2);
        assertThat(january.taxis().get(0).plate()).isEqualTo("AAA111");
        assertThat(january.taxis().get(0).totals().received()).isEqualByComparingTo("220");
        assertThat(january.totals().received()).isEqualByComparingTo("310");
        assertThat(january.totals().net()).isEqualByComparingTo("265"); // (85 + 105 + 75)
        assertThat(report.get(1).totals().received()).isEqualByComparingTo("110");
    }

    @Test
    void elReporteSemanalCubreTodoElAnioYRecortaLasSemanasQueCruzanDeAnio() {
        List<PeriodReport> report = aggregator.aggregate(List.of(), ReportPeriod.WEEK, 2026);

        // 2026 empieza en jueves y termina en jueves: 53 semanas (lunes a domingo) lo tocan
        assertThat(report).hasSize(53);
        assertThat(report.get(0).start()).isEqualTo(LocalDate.of(2026, 1, 1));   // recortada: la semana empieza el 29-dic
        assertThat(report.get(0).end()).isEqualTo(LocalDate.of(2026, 1, 4));
        assertThat(report.get(1).start()).isEqualTo(LocalDate.of(2026, 1, 5));
        assertThat(report.get(52).end()).isEqualTo(LocalDate.of(2026, 12, 31));  // recortada: la semana termina el 3-ene
    }

    @Test
    void semanasVanDeLunesADomingo() {
        List<DailyTotalsRow> rows = List.of(
                row(1, "AAA111", "2026-03-01", "100"),  // domingo -> semana que empieza el 23-feb
                row(1, "AAA111", "2026-03-02", "100"),  // lunes   -> nueva semana
                row(1, "AAA111", "2026-03-08", "100")); // domingo -> misma semana que el 2-mar

        List<PeriodReport> report = aggregator.aggregate(rows, ReportPeriod.WEEK, 2026);

        PeriodReport sundayWeek = report.stream().filter(p -> p.start().equals(LocalDate.of(2026, 2, 23))).findFirst()
                .orElseThrow();
        PeriodReport mondayWeek = report.stream().filter(p -> p.start().equals(LocalDate.of(2026, 3, 2))).findFirst()
                .orElseThrow();
        assertThat(sundayWeek.totals().received()).isEqualByComparingTo("100");
        assertThat(mondayWeek.end()).isEqualTo(LocalDate.of(2026, 3, 8));
        assertThat(mondayWeek.totals().received()).isEqualByComparingTo("200");
    }
}
