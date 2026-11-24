package com.flota.taxis.daily;

/** {@code updated} días cambiaron su indicador de Pico y Placa, de {@code total} revisados. */
public record RecalculateResult(int year, int updated, int total) {
}
