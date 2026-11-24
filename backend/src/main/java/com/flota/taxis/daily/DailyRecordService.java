package com.flota.taxis.daily;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flota.taxis.common.exception.BusinessRuleException;
import com.flota.taxis.common.exception.DuplicateResourceException;
import com.flota.taxis.common.exception.ResourceNotFoundException;
import com.flota.taxis.common.exception.StaleDataException;
import com.flota.taxis.picoplaca.PicoPlacaRule;
import com.flota.taxis.taxi.Taxi;
import com.flota.taxis.taxi.TaxiRepository;

/** Casos de uso del control diario. Todos reciben el propietario y solo tocan sus propios taxis. */
@Service
@Transactional(readOnly = true)
public class DailyRecordService {

    private static final int MAX_PAGE_SIZE = 400;

    private final DailyRecordRepository records;
    private final TaxiRepository taxis;
    private final PicoPlacaRule picoPlacaRule;

    public DailyRecordService(DailyRecordRepository records, TaxiRepository taxis, PicoPlacaRule picoPlacaRule) {
        this.records = records;
        this.taxis = taxis;
        this.picoPlacaRule = picoPlacaRule;
    }

    /** Lista paginada del control diario de un taxi, por año completo o por mes. */
    public Page<DailyRecordResponse> list(Long ownerId, Long taxiId, int year, Integer month, int page, int size) {
        if (!taxis.existsByIdAndOwnerId(taxiId, ownerId)) {
            throw new ResourceNotFoundException("Taxi", taxiId);
        }
        LocalDate from;
        LocalDate to;
        if (month == null) {
            from = LocalDate.of(year, 1, 1);
            to = LocalDate.of(year, 12, 31);
        } else {
            YearMonth ym = YearMonth.of(year, month);
            from = ym.atDay(1);
            to = ym.atEndOfMonth();
        }
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by("recordDate").ascending());
        return records.findByTaxiIdAndRecordDateBetween(taxiId, from, to, pageable).map(DailyRecordResponse::from);
    }

    /**
     * Crea el control diario del año completo para un taxi. Es idempotente:
     * los días que ya existen se omiten, por lo que se puede ejecutar de nuevo sin duplicar.
     */
    @Transactional
    public GenerateYearResult generateYear(Long ownerId, Long taxiId, int year, BigDecimal expectedProduction) {
        requireNonNegative(expectedProduction);
        Taxi taxi = loadTaxi(ownerId, taxiId);
        if (!taxi.isActive()) {
            throw new BusinessRuleException("No se puede generar el control diario de un taxi inactivo");
        }

        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);
        Set<LocalDate> existing = new HashSet<>(records.findExistingDates(taxiId, start, end));

        List<DailyRecord> toCreate = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            if (!existing.contains(date)) {
                boolean picoPlaca = picoPlacaRule.applies(taxi.getPlate(), date);
                toCreate.add(DailyRecord.open(taxi, date, picoPlaca, expectedProduction));
            }
        }
        records.saveAll(toCreate);
        return new GenerateYearResult(year, toCreate.size(), existing.size());
    }

    /** Agrega un día suelto (por ejemplo uno que se borró o que quedó fuera del año generado). */
    @Transactional
    public DailyRecordResponse create(Long ownerId, Long taxiId, CreateDailyRecordRequest request) {
        Taxi taxi = loadTaxi(ownerId, taxiId);
        if (!taxi.isActive()) {
            throw new BusinessRuleException("No se puede agregar días a un taxi inactivo");
        }
        LocalDate date = request.date();
        if (records.existsByTaxiIdAndRecordDate(taxiId, date)) {
            throw new DuplicateResourceException(
                    "El taxi %s ya tiene un registro para el %s".formatted(taxi.getPlate(), date));
        }
        boolean picoPlaca = picoPlacaRule.applies(taxi.getPlate(), date);
        DailyRecord record = records.save(DailyRecord.open(taxi, date, picoPlaca, request.expectedProduction()));
        records.flush();
        return DailyRecordResponse.from(record);
    }

    @Transactional
    public DailyRecordResponse update(Long ownerId, Long id, UpdateDailyRecordRequest request) {
        DailyRecord record = loadWithVersionCheck(ownerId, id, request.version());

        boolean picoPlaca = request.picoPlaca() != null ? request.picoPlaca() : record.isPicoPlaca();
        record.register(picoPlaca, request.expectedProduction(), request.receivedProduction(),
                request.driverSavings(), request.extraExpenses(), request.status(), request.notes());

        // flush para obtener la nueva versión y detectar conflictos aquí, no al cerrar la transacción
        records.flush();
        return DailyRecordResponse.from(record);
    }

    @Transactional
    public DailyRecordResponse changeStatus(Long ownerId, Long id, ChangeStatusRequest request) {
        DailyRecord record = loadWithVersionCheck(ownerId, id, request.version());
        record.changeStatus(request.status());
        records.flush();
        return DailyRecordResponse.from(record);
    }

    @Transactional
    public void delete(Long ownerId, Long id) {
        DailyRecord record = records.findByIdAndTaxiOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro diario", id));
        records.delete(record);
    }

    /**
     * Vuelve a calcular el Pico y Placa de los días ya creados con la regla vigente. Útil tras
     * corregir la configuración. {@code expectedProduction} se usa solo en los días sin movimiento
     * que dejan de ser Pico y Placa (los que sí lo son quedan con producción esperada cero).
     */
    @Transactional
    public RecalculateResult recalculatePicoPlaca(Long ownerId, Long taxiId, int year, BigDecimal expectedProduction) {
        requireNonNegative(expectedProduction);
        Taxi taxi = loadTaxi(ownerId, taxiId);
        List<DailyRecord> days = records.findAllByTaxiIdAndRecordDateBetween(
                taxiId, LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));

        int updated = 0;
        for (DailyRecord day : days) {
            boolean shouldApply = picoPlacaRule.applies(taxi.getPlate(), day.getRecordDate());
            if (shouldApply != day.isPicoPlaca()) {
                day.reapplyPicoPlaca(shouldApply, expectedProduction);
                updated++;
            }
        }
        return new RecalculateResult(year, updated, days.size());
    }

    private Taxi loadTaxi(Long ownerId, Long taxiId) {
        return taxis.findByIdAndOwnerId(taxiId, ownerId).orElseThrow(() -> new ResourceNotFoundException("Taxi", taxiId));
    }

    private DailyRecord loadWithVersionCheck(Long ownerId, Long id, Long expectedVersion) {
        DailyRecord record = records.findByIdAndTaxiOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro diario", id));
        if (!record.getVersion().equals(expectedVersion)) {
            throw new StaleDataException(
                    "Este registro fue modificado por otra persona. Recarga la tabla e inténtalo de nuevo");
        }
        return record;
    }

    private static void requireNonNegative(BigDecimal expectedProduction) {
        if (expectedProduction == null || expectedProduction.signum() < 0) {
            throw new BusinessRuleException("La producción esperada no puede ser negativa");
        }
    }
}
