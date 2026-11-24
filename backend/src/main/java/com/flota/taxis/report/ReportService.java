package com.flota.taxis.report;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flota.taxis.daily.DailyRecordRepository;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final DailyRecordRepository records;
    private final ReportAggregator aggregator;

    public ReportService(DailyRecordRepository records, ReportAggregator aggregator) {
        this.records = records;
        this.aggregator = aggregator;
    }

    /** Todos los periodos (semanas o meses) del año para los taxis del propietario. */
    public List<PeriodReport> report(Long ownerId, ReportPeriod period, int year) {
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to = LocalDate.of(year, 12, 31);
        return aggregator.aggregate(records.findDailyTotals(ownerId, from, to), period, year);
    }
}
