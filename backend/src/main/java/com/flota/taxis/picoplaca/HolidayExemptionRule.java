package com.flota.taxis.picoplaca;

import java.time.LocalDate;
import java.util.Set;

/** Patrón Decorator: añade la exención por festivos a cualquier regla base. */
public class HolidayExemptionRule implements PicoPlacaRule {

    private final PicoPlacaRule delegate;
    private final HolidayCalendar holidays;

    public HolidayExemptionRule(PicoPlacaRule delegate, HolidayCalendar holidays) {
        this.delegate = delegate;
        this.holidays = holidays;
    }

    @Override
    public Set<Integer> restrictedDigits(LocalDate date) {
        return holidays.isHoliday(date) ? Set.of() : delegate.restrictedDigits(date);
    }
}
