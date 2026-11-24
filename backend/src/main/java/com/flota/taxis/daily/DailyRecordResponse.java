package com.flota.taxis.daily;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;

public record DailyRecordResponse(Long id, Long version, Long taxiId, String plate, LocalDate date,
        DayOfWeek dayOfWeek, boolean picoPlaca, BigDecimal expectedProduction, BigDecimal receivedProduction,
        BigDecimal driverSavings, BigDecimal extraExpenses, BigDecimal netDelivered, ComplianceStatus status,
        String notes) {

    public static DailyRecordResponse from(DailyRecord r) {
        return new DailyRecordResponse(r.getId(), r.getVersion(), r.getTaxi().getId(), r.getTaxi().getPlate(),
                r.getRecordDate(), r.getRecordDate().getDayOfWeek(), r.isPicoPlaca(), r.getExpectedProduction(),
                r.getReceivedProduction(), r.getDriverSavings(), r.getExtraExpenses(), r.getNetDelivered(),
                r.getStatus(), r.getNotes());
    }
}
