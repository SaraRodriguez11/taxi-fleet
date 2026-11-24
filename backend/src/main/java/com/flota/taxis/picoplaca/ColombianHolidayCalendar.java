package com.flota.taxis.picoplaca;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Festivos de Colombia calculados por reglas (Ley 51 de 1983, "Ley Emiliani"), para que
 * funcionen en cualquier año sin mantener una lista a mano:
 * <ul>
 *   <li>Fijos: 1-ene, 1-may, 20-jul, 7-ago, 8-dic, 25-dic.</li>
 *   <li>Trasladados al lunes siguiente: 6-ene, 19-mar, 29-jun, 15-ago, 12-oct, 1-nov, 11-nov.</li>
 *   <li>Según la Semana Santa: Jueves y Viernes Santo (fijos) y Ascensión, Corpus Christi y
 *       Sagrado Corazón (trasladados al lunes).</li>
 * </ul>
 * Se pueden agregar festivos extra (decretos especiales) por configuración.
 */
public class ColombianHolidayCalendar implements HolidayCalendar {

    private final Set<LocalDate> extraHolidays;
    private final Map<Integer, Set<LocalDate>> cache = new ConcurrentHashMap<>();

    public ColombianHolidayCalendar(Set<LocalDate> extraHolidays) {
        this.extraHolidays = Set.copyOf(extraHolidays);
    }

    @Override
    public boolean isHoliday(LocalDate date) {
        return extraHolidays.contains(date) || cache.computeIfAbsent(date.getYear(), ColombianHolidayCalendar::compute).contains(date);
    }

    static Set<LocalDate> compute(int year) {
        Set<LocalDate> holidays = new HashSet<>();

        // Fijos
        holidays.add(LocalDate.of(year, Month.JANUARY, 1));
        holidays.add(LocalDate.of(year, Month.MAY, 1));
        holidays.add(LocalDate.of(year, Month.JULY, 20));
        holidays.add(LocalDate.of(year, Month.AUGUST, 7));
        holidays.add(LocalDate.of(year, Month.DECEMBER, 8));
        holidays.add(LocalDate.of(year, Month.DECEMBER, 25));

        // Trasladados al lunes siguiente (si ya es lunes, se queda)
        holidays.add(toMonday(LocalDate.of(year, Month.JANUARY, 6)));
        holidays.add(toMonday(LocalDate.of(year, Month.MARCH, 19)));
        holidays.add(toMonday(LocalDate.of(year, Month.JUNE, 29)));
        holidays.add(toMonday(LocalDate.of(year, Month.AUGUST, 15)));
        holidays.add(toMonday(LocalDate.of(year, Month.OCTOBER, 12)));
        holidays.add(toMonday(LocalDate.of(year, Month.NOVEMBER, 1)));
        holidays.add(toMonday(LocalDate.of(year, Month.NOVEMBER, 11)));

        // Dependientes de la Semana Santa
        LocalDate easter = easterSunday(year);
        holidays.add(easter.minusDays(3));              // Jueves Santo
        holidays.add(easter.minusDays(2));              // Viernes Santo
        holidays.add(toMonday(easter.plusDays(39)));    // Ascensión del Señor
        holidays.add(toMonday(easter.plusDays(60)));    // Corpus Christi
        holidays.add(toMonday(easter.plusDays(68)));    // Sagrado Corazón

        return Set.copyOf(holidays);
    }

    private static LocalDate toMonday(LocalDate date) {
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
    }

    /** Domingo de Pascua (algoritmo gregoriano anónimo, de Meeus/Jones/Butcher). */
    static LocalDate easterSunday(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(year, month, day);
    }
}
