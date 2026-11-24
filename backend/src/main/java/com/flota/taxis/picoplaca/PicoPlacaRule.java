package com.flota.taxis.picoplaca;

import java.time.LocalDate;
import java.util.Set;

/**
 * Patrón Strategy: abstrae "¿qué dígitos finales de placa tienen restricción en esta fecha?".
 * Permite sustituir la normativa (otra ciudad, otro semestre) sin tocar los servicios.
 */
public interface PicoPlacaRule {

    /** Dígitos finales de placa con restricción en la fecha (vacío si ese día no hay restricción). */
    Set<Integer> restrictedDigits(LocalDate date);

    /** ¿Le aplica Pico y Placa a esta placa en esta fecha? Se decide por su último dígito. */
    default boolean applies(String plate, LocalDate date) {
        if (plate == null || plate.isBlank()) {
            return false;
        }
        int lastDigit = Character.digit(plate.charAt(plate.length() - 1), 10);
        return lastDigit >= 0 && restrictedDigits(date).contains(lastDigit);
    }
}
