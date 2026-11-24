package com.flota.taxis.picoplaca;

import java.util.HashSet;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.flota.taxis.config.FleetProperties;

/**
 * Composición de la regla de Pico y Placa: la rotación de dígitos se envuelve con un
 * decorador que exime los festivos. Cambiar la normativa = cambiar el YAML (o añadir otra
 * estrategia/decorador) sin modificar los servicios.
 */
@Configuration
public class PicoPlacaConfig {

    @Bean
    PicoPlacaRule picoPlacaRule(FleetProperties properties) {
        FleetProperties.PicoPlaca config = properties.picoPlaca();
        PicoPlacaRule rotation = new RotatingDigitRule(config.anchorDate(), config.anchorDigits(), config.groups());
        return new HolidayExemptionRule(rotation, new ColombianHolidayCalendar(new HashSet<>(config.extraHolidays())));
    }
}
