package com.flota.taxis.picoplaca;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Rotación de dígitos como la de los taxis de Bogotá: los dígitos se agrupan en pares
 * (1-2, 3-4, 5-6, 7-8, 9-0) y el par restringido avanza un paso cada día de lunes a sábado,
 * de forma cíclica. Los domingos no hay restricción y no avanzan la rotación.
 *
 * <p>Los festivos que caen de lunes a sábado SÍ consumen un paso (el día se salta, pero la
 * rotación sigue); por eso la exención de festivos se aplica aparte, con
 * {@link HolidayExemptionRule}, y no dentro de esta clase.
 *
 * <p>La rotación se calcula a partir de un día "ancla" conocido, de modo que sirve para
 * cualquier fecha (pasada o futura) sin tabla. Si la autoridad reinicia el ciclo, basta con
 * mover el ancla en la configuración.
 */
public class RotatingDigitRule implements PicoPlacaRule {

    private static final int OPERATING_DAYS_PER_WEEK = 6; // lunes a sábado

    private final List<Set<Integer>> groups;
    private final long anchorOrdinal;
    private final int anchorIndex;

    public RotatingDigitRule(LocalDate anchorDate, Set<Integer> anchorDigits, List<Set<Integer>> groups) {
        if (groups == null || groups.isEmpty()) {
            throw new IllegalArgumentException("Debe haber al menos un grupo de dígitos");
        }
        if (anchorDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new IllegalArgumentException("La fecha ancla no puede ser domingo: ese día no hay restricción");
        }
        this.groups = groups.stream().map(Set::copyOf).toList();
        this.anchorIndex = this.groups.indexOf(Set.copyOf(anchorDigits));
        if (anchorIndex < 0) {
            throw new IllegalArgumentException(
                    "Los dígitos del ancla %s no coinciden con ningún grupo: %s".formatted(anchorDigits, groups));
        }
        this.anchorOrdinal = ordinal(anchorDate);
    }

    @Override
    public Set<Integer> restrictedDigits(LocalDate date) {
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return Set.of();
        }
        long steps = ordinal(date) - anchorOrdinal;
        int index = (int) Math.floorMod(anchorIndex + steps, (long) groups.size());
        return groups.get(index);
    }

    /**
     * Número de orden del día entre los días lunes-sábado (el domingo comparte el de su sábado).
     * El 1-ene-1970 fue jueves, por eso se suma 3 para alinear las semanas a partir del lunes.
     */
    private static long ordinal(LocalDate date) {
        long week = Math.floorDiv(date.toEpochDay() + 3, 7);
        int dayIndex = Math.min(date.getDayOfWeek().getValue() - 1, OPERATING_DAYS_PER_WEEK - 1);
        return week * OPERATING_DAYS_PER_WEEK + dayIndex;
    }
}
