package com.flota.taxis.report;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

/**
 * Patrón Strategy (vía enum): cada periodo sabe a qué ventana pertenece una fecha.
 * Añadir "trimestral" o "quincenal" es agregar un valor, sin tocar el agregador.
 */
public enum ReportPeriod {

    WEEK {
        @Override
        public PeriodWindow windowFor(LocalDate date) {
            LocalDate start = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            int isoWeek = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            return new PeriodWindow("Semana " + isoWeek, start, start.plusDays(6));
        }
    },

    MONTH {
        @Override
        public PeriodWindow windowFor(LocalDate date) {
            String month = date.getMonth().getDisplayName(java.time.format.TextStyle.FULL, Locale.forLanguageTag("es-CO"));
            String label = Character.toUpperCase(month.charAt(0)) + month.substring(1);
            return new PeriodWindow(label, date.withDayOfMonth(1), date.with(TemporalAdjusters.lastDayOfMonth()));
        }
    };

    public abstract PeriodWindow windowFor(LocalDate date);
}
