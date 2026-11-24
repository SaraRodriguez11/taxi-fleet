package com.flota.taxis.daily;

import java.math.BigDecimal;

import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flota.taxis.auth.AuthenticatedOwner;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api")
public class DailyRecordController {

    private final DailyRecordService service;

    public DailyRecordController(DailyRecordService service) {
        this.service = service;
    }

    @GetMapping("/taxis/{taxiId}/daily-records")
    public PagedModel<DailyRecordResponse> list(
            @AuthenticationPrincipal AuthenticatedOwner owner,
            @PathVariable Long taxiId,
            @RequestParam @Min(2000) @Max(2100) int year,
            @RequestParam(required = false) @Min(1) @Max(12) Integer month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return new PagedModel<>(service.list(owner.id(), taxiId, year, month, page, size));
    }

    @PostMapping("/taxis/{taxiId}/daily-records/generate")
    public GenerateYearResult generateYear(
            @AuthenticationPrincipal AuthenticatedOwner owner,
            @PathVariable Long taxiId,
            @RequestParam @Min(2000) @Max(2100) int year,
            @RequestParam BigDecimal expectedProduction) {
        return service.generateYear(owner.id(), taxiId, year, expectedProduction);
    }

    @PostMapping("/taxis/{taxiId}/daily-records")
    public ResponseEntity<DailyRecordResponse> create(@AuthenticationPrincipal AuthenticatedOwner owner,
            @PathVariable Long taxiId, @Valid @RequestBody CreateDailyRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(owner.id(), taxiId, request));
    }

    @PostMapping("/taxis/{taxiId}/daily-records/recalculate-pico-placa")
    public RecalculateResult recalculatePicoPlaca(
            @AuthenticationPrincipal AuthenticatedOwner owner,
            @PathVariable Long taxiId,
            @RequestParam @Min(2000) @Max(2100) int year,
            @RequestParam BigDecimal expectedProduction) {
        return service.recalculatePicoPlaca(owner.id(), taxiId, year, expectedProduction);
    }

    @PutMapping("/daily-records/{id}")
    public DailyRecordResponse update(@AuthenticationPrincipal AuthenticatedOwner owner, @PathVariable Long id,
            @Valid @RequestBody UpdateDailyRecordRequest request) {
        return service.update(owner.id(), id, request);
    }

    @PatchMapping("/daily-records/{id}/status")
    public DailyRecordResponse changeStatus(@AuthenticationPrincipal AuthenticatedOwner owner, @PathVariable Long id,
            @Valid @RequestBody ChangeStatusRequest request) {
        return service.changeStatus(owner.id(), id, request);
    }

    @DeleteMapping("/daily-records/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedOwner owner, @PathVariable Long id) {
        service.delete(owner.id(), id);
        return ResponseEntity.noContent().build();
    }
}
