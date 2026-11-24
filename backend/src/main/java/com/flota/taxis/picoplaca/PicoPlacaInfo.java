package com.flota.taxis.picoplaca;

import java.time.LocalDate;
import java.util.List;

/** Dígitos finales de placa con restricción en una fecha (lista vacía = no hay restricción). */
public record PicoPlacaInfo(LocalDate date, List<Integer> restrictedDigits) {
}
