package com.flota.taxis.daily;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import com.flota.taxis.common.exception.BusinessRuleException;
import com.flota.taxis.taxi.Taxi;

/**
 * Registro contable de un taxi en un día. Es un modelo de dominio rico:
 * el neto entregado y sus invariantes viven aquí, no en los controladores.
 */
@Entity
@Table(name = "daily_records",
        uniqueConstraints = @UniqueConstraint(name = "uk_daily_taxi_date", columnNames = { "taxi_id", "record_date" }))
public class DailyRecord {

    private static final int SCALE = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "daily_record_seq")
    @SequenceGenerator(name = "daily_record_seq", sequenceName = "daily_record_seq", allocationSize = 50)
    private Long id;

    /** Control de concurrencia optimista (dos administradores editando el mismo día). */
    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "taxi_id", nullable = false)
    private Taxi taxi;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "pico_placa", nullable = false)
    private boolean picoPlaca;

    @Column(name = "expected_production", nullable = false, precision = 14, scale = 2)
    private BigDecimal expectedProduction;

    @Column(name = "received_production", nullable = false, precision = 14, scale = 2)
    private BigDecimal receivedProduction;

    @Column(name = "driver_savings", nullable = false, precision = 14, scale = 2)
    private BigDecimal driverSavings;

    @Column(name = "extra_expenses", nullable = false, precision = 14, scale = 2)
    private BigDecimal extraExpenses;

    @Column(name = "net_delivered", nullable = false, precision = 14, scale = 2)
    private BigDecimal netDelivered;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ComplianceStatus status;

    @Column(length = 500)
    private String notes;

    protected DailyRecord() {
        // requerido por JPA
    }

    /** Abre el día en blanco. En Pico y Placa no se espera producción. */
    public static DailyRecord open(Taxi taxi, LocalDate date, boolean picoPlaca, BigDecimal expectedProduction) {
        DailyRecord record = new DailyRecord();
        record.taxi = taxi;
        record.recordDate = date;
        record.picoPlaca = picoPlaca;
        record.expectedProduction = picoPlaca ? money(BigDecimal.ZERO) : money(expectedProduction);
        record.receivedProduction = money(BigDecimal.ZERO);
        record.driverSavings = money(BigDecimal.ZERO);
        record.extraExpenses = money(BigDecimal.ZERO);
        record.netDelivered = money(BigDecimal.ZERO);
        record.status = ComplianceEvaluator.evaluate(picoPlaca, record.expectedProduction, record.receivedProduction);
        return record;
    }

    /**
     * Registra los valores del día y recalcula el neto:
     * {@code neto = producción recibida - ahorro - gastos extra}.
     */
    public void register(boolean picoPlaca, BigDecimal expected, BigDecimal received, BigDecimal savings,
            BigDecimal extra, ComplianceStatus status, String notes) {
        requireNonNegative(expected, "La producción esperada");
        requireNonNegative(received, "La producción recibida");
        requireNonNegative(savings, "El ahorro del conductor");
        requireNonNegative(extra, "Los gastos extra");

        BigDecimal net = money(received).subtract(money(savings)).subtract(money(extra));
        if (net.signum() < 0) {
            throw new BusinessRuleException(
                    "El ahorro y los gastos extra no pueden superar la producción recibida");
        }

        this.picoPlaca = picoPlaca;
        this.expectedProduction = money(expected);
        this.receivedProduction = money(received);
        this.driverSavings = money(savings);
        this.extraExpenses = money(extra);
        this.netDelivered = net;
        this.status = status != null
                ? status
                : ComplianceEvaluator.evaluate(picoPlaca, this.expectedProduction, this.receivedProduction);
        this.notes = notes == null || notes.isBlank() ? null : notes.trim();
    }

    /** Cambio manual del estado (por ejemplo desde el selector de la tabla). Al marcar Entregado, completa lo recibido. */
    public void changeStatus(ComplianceStatus newStatus) {
        if (newStatus == null) {
            throw new BusinessRuleException("El estado es obligatorio");
        }
        // Entregado = la producción esperada llegó completa: se registra como recibida y se recalcula el neto
        if (newStatus == ComplianceStatus.CUMPLIDO && receivedProduction.compareTo(expectedProduction) < 0) {
            BigDecimal net = expectedProduction.subtract(driverSavings).subtract(extraExpenses);
            if (net.signum() < 0) {
                throw new BusinessRuleException(
                    "El ahorro y los gastos extra no pueden superar la producción recibida");
            }
            this.receivedProduction = expectedProduction;
            this.netDelivered = net;
        }
        this.status = newStatus;
    }

    /**
     * Corrige el Pico y Placa de un día ya creado (por ejemplo tras ajustar la regla).
     * Si el día aún no tiene movimiento ni estado manual, también se reajusta la producción esperada
     * (cero en Pico y Placa) y el estado; si ya tiene datos registrados, solo se cambia el indicador
     * para no tocar dinero ya capturado.
     */
    public void reapplyPicoPlaca(boolean newPicoPlaca, BigDecimal defaultExpected) {
        boolean untouched = isUntouched();
        this.picoPlaca = newPicoPlaca;
        if (untouched) {
            this.expectedProduction = newPicoPlaca ? money(BigDecimal.ZERO) : money(defaultExpected);
            this.status = ComplianceEvaluator.evaluate(newPicoPlaca, this.expectedProduction, this.receivedProduction);
        }
    }

    /** Sin dinero capturado, sin notas y con el estado que el sistema habría calculado solo. */
    private boolean isUntouched() {
        return receivedProduction.signum() == 0
                && driverSavings.signum() == 0
                && extraExpenses.signum() == 0
                && notes == null
                && status == ComplianceEvaluator.evaluate(picoPlaca, expectedProduction, receivedProduction);
    }

    private static void requireNonNegative(BigDecimal value, String label) {
        if (value == null || value.signum() < 0) {
            throw new BusinessRuleException(label + " no puede ser negativo");
        }
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public Long getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public Taxi getTaxi() {
        return taxi;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public boolean isPicoPlaca() {
        return picoPlaca;
    }

    public BigDecimal getExpectedProduction() {
        return expectedProduction;
    }

    public BigDecimal getReceivedProduction() {
        return receivedProduction;
    }

    public BigDecimal getDriverSavings() {
        return driverSavings;
    }

    public BigDecimal getExtraExpenses() {
        return extraExpenses;
    }

    public BigDecimal getNetDelivered() {
        return netDelivered;
    }

    public ComplianceStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }
}
