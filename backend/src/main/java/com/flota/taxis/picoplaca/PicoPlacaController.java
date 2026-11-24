package com.flota.taxis.picoplaca;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pico-placa")
public class PicoPlacaController {

    private final PicoPlacaRule rule;

    public PicoPlacaController(PicoPlacaRule rule) {
        this.rule = rule;
    }

    /** Sin parámetro devuelve la restricción de hoy. */
    @GetMapping
    public PicoPlacaInfo byDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate day = date != null ? date : LocalDate.now();
        List<Integer> digits = rule.restrictedDigits(day).stream().sorted().toList();
        return new PicoPlacaInfo(day, digits);
    }
}
