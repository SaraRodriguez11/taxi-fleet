package com.flota.taxis.daily;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Agrega un día suelto al control de un taxi. El Pico y Placa lo decide el servidor. */
public record CreateDailyRecordRequest(
        @NotNull(message = "La fecha es obligatoria") LocalDate date,
        @NotNull(message = "La producción esperada es obligatoria")
        @PositiveOrZero(message = "La producción esperada no puede ser negativa")
        @Digits(integer = 12, fraction = 2, message = "Formato de monto inválido")
        BigDecimal expectedProduction) {
}
