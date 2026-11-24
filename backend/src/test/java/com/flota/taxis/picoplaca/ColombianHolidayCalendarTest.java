package com.flota.taxis.picoplaca;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class ColombianHolidayCalendarTest {

    private final ColombianHolidayCalendar calendar = new ColombianHolidayCalendar(Set.of());

    @Test
    void festivosDe2026() {
        List<LocalDate> expected = List.of(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 12), LocalDate.of(2026, 3, 23),
                LocalDate.of(2026, 4, 2), LocalDate.of(2026, 4, 3), LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 18), LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 29), LocalDate.of(2026, 7, 20), LocalDate.of(2026, 8, 7),
                LocalDate.of(2026, 8, 17), LocalDate.of(2026, 10, 12), LocalDate.of(2026, 11, 2),
                LocalDate.of(2026, 11, 16), LocalDate.of(2026, 12, 8), LocalDate.of(2026, 12, 25));

        List<LocalDate> actual = LocalDate.of(2026, 1, 1).datesUntil(LocalDate.of(2027, 1, 1))
                .filter(calendar::isHoliday)
                .toList();

        assertThat(actual).containsExactlyElementsOf(expected);
    }

    @Test
    void semanaSantaSeCalculaCadaAnio() {
        assertThat(calendar.isHoliday(LocalDate.of(2025, 4, 18))).isTrue(); // Viernes Santo 2025
        assertThat(calendar.isHoliday(LocalDate.of(2027, 3, 26))).isTrue(); // Viernes Santo 2027
    }

    @Test
    void aceptaFestivosExtraordinarios() {
        LocalDate special = LocalDate.of(2026, 9, 9);
        assertThat(calendar.isHoliday(special)).isFalse();
        assertThat(new ColombianHolidayCalendar(Set.of(special)).isHoliday(special)).isTrue();
    }
}
