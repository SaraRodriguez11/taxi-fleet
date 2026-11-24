package com.flota.taxis.daily;

import jakarta.validation.constraints.NotNull;

public record ChangeStatusRequest(
        @NotNull(message = "La versión es obligatoria") Long version,
        @NotNull(message = "El estado es obligatorio") ComplianceStatus status) {
}
