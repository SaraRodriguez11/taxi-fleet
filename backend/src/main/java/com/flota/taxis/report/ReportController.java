package com.flota.taxis.report;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flota.taxis.auth.AuthenticatedOwner;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping("/weekly")
    public List<PeriodReport> weekly(@AuthenticationPrincipal AuthenticatedOwner owner,
            @RequestParam @Min(2000) @Max(2100) int year) {
        return service.report(owner.id(), ReportPeriod.WEEK, year);
    }

    @GetMapping("/monthly")
    public List<PeriodReport> monthly(@AuthenticationPrincipal AuthenticatedOwner owner,
            @RequestParam @Min(2000) @Max(2100) int year) {
        return service.report(owner.id(), ReportPeriod.MONTH, year);
    }
}
