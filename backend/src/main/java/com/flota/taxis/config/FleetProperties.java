package com.flota.taxis.config;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de negocio tipada (fail-fast al arrancar si el YAML es inválido).
 * La normativa de Pico y Placa, y la seguridad viven aquí y no en el código.
 */
@ConfigurationProperties(prefix = "fleet")
public record FleetProperties(Cors cors, PicoPlaca picoPlaca, Security security) {

    public FleetProperties {
        cors = cors == null ? new Cors(List.of()) : cors;
        picoPlaca = picoPlaca == null ? PicoPlaca.bogotaTaxis() : picoPlaca;
        security = security == null ? new Security(null, null) : security;
    }

    public record Cors(List<String> allowedOrigins) {
        public Cors {
            allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
        }
    }

    /** Clave para firmar los tokens JWT (mínimo 32 caracteres) y su duración. */
    public record Security(String jwtSecret, Duration jwtTtl) {
        public Security {
            if (jwtSecret == null || jwtSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
                throw new IllegalStateException(
                        "fleet.security.jwt-secret debe tener al menos 32 caracteres (variable de entorno JWT_SECRET)");
            }
            jwtTtl = jwtTtl == null ? Duration.ofHours(8) : jwtTtl;
        }
    }

    /**
     * Rotación de dígitos: {@code groups} define el orden del ciclo y {@code anchorDigits} indica
     * qué grupo restringe en {@code anchorDate}. {@code extraHolidays} añade festivos especiales
     * a los que ya calcula el calendario colombiano.
     */
    public record PicoPlaca(LocalDate anchorDate, Set<Integer> anchorDigits, List<Set<Integer>> groups,
            List<LocalDate> extraHolidays) {

        public PicoPlaca {
            if (anchorDate == null || anchorDigits == null || groups == null) {
                PicoPlaca defaults = bogotaTaxis();
                anchorDate = anchorDate == null ? defaults.anchorDate : anchorDate;
                anchorDigits = anchorDigits == null ? defaults.anchorDigits : anchorDigits;
                groups = groups == null ? defaults.groups : groups;
            }
            anchorDigits = Set.copyOf(anchorDigits);
            groups = groups.stream().map(Set::copyOf).toList();
            extraHolidays = extraHolidays == null ? List.of() : List.copyOf(extraHolidays);
        }

        static PicoPlaca bogotaTaxis() {
            return new PicoPlaca(LocalDate.of(2026, 11, 5), Set.of(5, 6),
                    List.of(Set.of(1, 2), Set.of(3, 4), Set.of(5, 6), Set.of(7, 8), Set.of(9, 0)), List.of());
        }
    }
}
