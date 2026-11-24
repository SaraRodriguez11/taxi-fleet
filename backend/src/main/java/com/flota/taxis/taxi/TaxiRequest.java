package com.flota.taxis.taxi;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** El propietario no se envía: es siempre la cuenta con sesión iniciada. */
public record TaxiRequest(
        @NotBlank(message = "La placa es obligatoria")
        @Pattern(regexp = "^[A-Za-z]{3}\\d{3}$", message = "La placa debe tener 3 letras y 3 números (ej. ABC123)")
        String plate,

        @NotBlank(message = "El modelo es obligatorio")
        @Size(max = 80, message = "El modelo admite máximo 80 caracteres")
        String model,

        Long driverId,

        Boolean active) {
}
