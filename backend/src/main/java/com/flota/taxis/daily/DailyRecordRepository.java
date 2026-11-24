package com.flota.taxis.daily;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Los registros pertenecen a un taxi y el taxi a un propietario. Las consultas por id o agregadas
 * exigen el propietario; las que reciben un taxi se usan solo después de verificar que es suyo.
 */
public interface DailyRecordRepository extends JpaRepository<DailyRecord, Long> {

    @EntityGraph(attributePaths = "taxi")
    Page<DailyRecord> findByTaxiIdAndRecordDateBetween(Long taxiId, LocalDate from, LocalDate to, Pageable pageable);

    @EntityGraph(attributePaths = "taxi")
    Optional<DailyRecord> findByIdAndTaxiOwnerId(Long id, Long ownerId);

    boolean existsByTaxiIdAndRecordDate(Long taxiId, LocalDate recordDate);

    List<DailyRecord> findAllByTaxiIdAndRecordDateBetween(Long taxiId, LocalDate from, LocalDate to);

    @Query("""
            select d.recordDate from DailyRecord d
            where d.taxi.id = :taxiId and d.recordDate between :from and :to
            """)
    List<LocalDate> findExistingDates(@Param("taxiId") Long taxiId, @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    /** Totales de la flota del propietario calculados en la base de datos (una sola consulta agregada). */
    @Query("""
            select new com.flota.taxis.daily.Totals(
                sum(d.expectedProduction), sum(d.receivedProduction),
                sum(d.driverSavings), sum(d.extraExpenses), sum(d.netDelivered))
            from DailyRecord d
            where d.taxi.owner.id = :ownerId and d.recordDate between :from and :to
            """)
    Totals sumTotals(@Param("ownerId") Long ownerId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    /** Filas diarias ligeras para agrupar por semana o mes en {@code ReportAggregator}. */
    @Query("""
            select new com.flota.taxis.daily.DailyTotalsRow(
                t.id, t.plate, d.recordDate, d.expectedProduction, d.receivedProduction,
                d.driverSavings, d.extraExpenses, d.netDelivered)
            from DailyRecord d join d.taxi t
            where t.owner.id = :ownerId and d.recordDate between :from and :to
            order by d.recordDate, t.plate
            """)
    List<DailyTotalsRow> findDailyTotals(@Param("ownerId") Long ownerId, @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
