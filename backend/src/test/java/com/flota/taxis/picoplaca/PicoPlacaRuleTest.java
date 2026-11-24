package com.flota.taxis.picoplaca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Los valores esperados salen de calendarios publicados para taxis de Bogotá
 * (boletines de bogota.gov.co y pyphoy.com): octubre 2026 completo y fechas sueltas de otros meses.
 */
class PicoPlacaRuleTest {

    private static final List<Set<Integer>> GROUPS =
            List.of(Set.of(1, 2), Set.of(3, 4), Set.of(5, 6), Set.of(7, 8), Set.of(9, 0));

    private final PicoPlacaRule rotation = new RotatingDigitRule(LocalDate.of(2026, 11, 5), Set.of(5, 6), GROUPS);
    private final PicoPlacaRule rule = new HolidayExemptionRule(rotation, new ColombianHolidayCalendar(Set.of()));

    private Set<Integer> on(String date) {
        return rule.restrictedDigits(LocalDate.parse(date));
    }

    @Test
    void coincideConElCalendarioPublicadoDeOctubre2026() {
        assertThat(on("2026-10-01")).containsExactlyInAnyOrder(5, 6);
        assertThat(on("2026-10-02")).containsExactlyInAnyOrder(7, 8);
        assertThat(on("2026-10-03")).containsExactlyInAnyOrder(9, 0);
        assertThat(on("2026-10-05")).containsExactlyInAnyOrder(1, 2);
        assertThat(on("2026-10-13")).containsExactlyInAnyOrder(5, 6);
        assertThat(on("2026-10-24")).containsExactlyInAnyOrder(5, 6);
        assertThat(on("2026-10-26")).containsExactlyInAnyOrder(7, 8);
        assertThat(on("2026-10-31")).containsExactlyInAnyOrder(7, 8);
    }

    @Test
    void coincideConFechasPublicadasDeOtrosMeses() {
        assertThat(on("2026-02-25")).containsExactlyInAnyOrder(1, 2);
        assertThat(on("2026-06-19")).containsExactlyInAnyOrder(7, 8);
        assertThat(on("2026-07-28")).containsExactlyInAnyOrder(3, 4);
        assertThat(on("2026-09-25")).containsExactlyInAnyOrder(5, 6);
        assertThat(on("2026-11-05")).containsExactlyInAnyOrder(5, 6);
    }

    @Test
    void domingosNoTienenRestriccionNiAvanzanLaRotacion() {
        assertThat(on("2026-10-04")).isEmpty(); // domingo
        // sábado 3 (9,0) -> lunes 5 (1,2): el domingo no consumió ningún paso
        assertThat(on("2026-10-03")).containsExactlyInAnyOrder(9, 0);
        assertThat(on("2026-10-05")).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void festivosNoTienenRestriccionPeroSiAvanzanLaRotacion() {
        assertThat(on("2026-10-12")).isEmpty(); // festivo (lunes)
        // sábado 10 (1,2) -> martes 13 (5,6): el festivo del 12 consumió el par 3,4
        assertThat(on("2026-10-10")).containsExactlyInAnyOrder(1, 2);
        assertThat(on("2026-10-13")).containsExactlyInAnyOrder(5, 6);
    }

    @Test
    void aplicaSegunElUltimoDigitoDeLaPlaca() {
        LocalDate day = LocalDate.of(2026, 11, 5); // restringen 5 y 6
        assertThat(rule.applies("ABC125", day)).isTrue();
        assertThat(rule.applies("ABC126", day)).isTrue();
        assertThat(rule.applies("ABC123", day)).isFalse();
        assertThat(rule.applies("ABC120", day)).isFalse();
    }

    @Test
    void placaInvalidaNuncaAplica() {
        LocalDate day = LocalDate.of(2026, 11, 5);
        assertThat(rule.applies("", day)).isFalse();
        assertThat(rule.applies(null, day)).isFalse();
        assertThat(rule.applies("ABCXYZ", day)).isFalse();
    }

    @Test
    void cadaPlacaTieneRestriccionUnDiaDeCadaCincoOperativos() {
        LocalDate monday = LocalDate.of(2026, 11, 9); // semana sin festivos
        for (int lastDigit = 0; lastDigit <= 9; lastDigit++) {
            String plate = "ABC12" + lastDigit;
            long restrictedDays = monday.datesUntil(monday.plusDays(35))
                    .filter(day -> rotation.applies(plate, day))
                    .count();
            // 35 días = 5 semanas = 30 días operativos (lunes a sábado) => 6 vueltas del ciclo de 5
            assertThat(restrictedDays).isEqualTo(6);
        }
    }

    @Test
    void elAnclaNoPuedeSerDomingoNiTenerDigitosFueraDeLosGrupos() {
        assertThatThrownBy(() -> new RotatingDigitRule(LocalDate.of(2026, 11, 8), Set.of(5, 6), GROUPS))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RotatingDigitRule(LocalDate.of(2026, 11, 5), Set.of(5, 7), GROUPS))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
