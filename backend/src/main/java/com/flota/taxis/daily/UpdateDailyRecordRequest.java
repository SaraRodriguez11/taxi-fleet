package com.flota.taxis.daily;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * El neto NO se recibe: lo calcula el servidor. {@code version} permite detectar
 * ediciones concurrentes. {@code picoPlaca} y {@code status} son opcionales (si faltan,
 * se conserva el Pico y Placa y el estado se deriva automáticamente).
 */
public record UpdateDailyRecordRequest(
        @NotNull(message = "La versión es obligatoria") Long version,
        Boolean picoPlaca,
        @NotNull(message = "La producción esperada es obligatoria")
        @PositiveOrZero(message = "La producción esperada no puede ser negativa")
        @Digits(integer = 12, fraction = 2, message = "Formato de monto inválido")
        BigDecimal expectedProduction,
        @NotNull(message = "La producción recibida es obligatoria")
        @PositiveOrZero(message = "La producción recibida no puede ser negativa")
        @Digits(integer = 12, fraction = 2, message = "Formato de monto inválido")
        BigDecimal receivedProduction,
        @NotNull(message = "El ahorro es obligatorio")
        @PositiveOrZero(message = "El ahorro no puede ser negativo")
        @Digits(integer = 12, fraction = 2, message = "Formato de monto inválido")
        BigDecimal driverSavings,
        @NotNull(message = "Los gastos extra son obligatorios")
        @PositiveOrZero(message = "Los gastos extra no pueden ser negativos")
        @Digits(integer = 12, fraction = 2, message = "Formato de monto inválido")
        BigDecimal extraExpenses,
        ComplianceStatus status,
        @Size(max = 500, message = "Las notas admiten máximo 500 caracteres") String notes) {
}
