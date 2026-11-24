package com.flota.taxis.dashboard;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flota.taxis.auth.AuthenticatedOwner;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    /** Sin parámetros devuelve el año en curso. */
    @GetMapping("/summary")
    public FleetSummary summary(
            @AuthenticationPrincipal AuthenticatedOwner owner,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        int year = LocalDate.now().getYear();
        return service.summary(owner.id(), from != null ? from : LocalDate.of(year, 1, 1),
                to != null ? to : LocalDate.of(year, 12, 31));
    }
}
