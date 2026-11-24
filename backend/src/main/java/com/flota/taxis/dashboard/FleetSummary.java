package com.flota.taxis.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.flota.taxis.daily.Totals;

public record FleetSummary(LocalDate from, LocalDate to, long activeTaxis, long activeDrivers, Totals totals,
        BigDecimal complianceRate) {
}
