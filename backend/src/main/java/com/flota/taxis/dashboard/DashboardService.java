package com.flota.taxis.dashboard;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flota.taxis.common.exception.BusinessRuleException;
import com.flota.taxis.daily.DailyRecordRepository;
import com.flota.taxis.daily.Totals;
import com.flota.taxis.driver.DriverRepository;
import com.flota.taxis.taxi.TaxiRepository;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final DailyRecordRepository records;
    private final TaxiRepository taxis;
    private final DriverRepository drivers;

    public DashboardService(DailyRecordRepository records, TaxiRepository taxis, DriverRepository drivers) {
        this.records = records;
        this.taxis = taxis;
        this.drivers = drivers;
    }

    public FleetSummary summary(Long ownerId, LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BusinessRuleException("La fecha inicial no puede ser posterior a la final");
        }
        Totals totals = records.sumTotals(ownerId, from, to);
        return new FleetSummary(from, to, taxis.countByOwnerIdAndActiveTrue(ownerId),
                drivers.countByOwnerIdAndActiveTrue(ownerId), totals, complianceRate(totals));
    }

    /** Porcentaje de producción recibida frente a la esperada. */
    static BigDecimal complianceRate(Totals totals) {
        if (totals.expected().signum() == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return totals.received()
                .multiply(BigDecimal.valueOf(100))
                .divide(totals.expected(), 2, RoundingMode.HALF_UP);
    }
}
