package com.flota.taxis.driver;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DriverRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre admite máximo 120 caracteres")
        String fullName,

        @NotBlank(message = "El documento es obligatorio")
        @Size(max = 20, message = "El documento admite máximo 20 caracteres")
        String documentNumber,

        @Size(max = 20, message = "El teléfono admite máximo 20 caracteres")
        String phone,

        Boolean active) {
}
