package com.flota.taxis.taxi;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flota.taxis.auth.AuthenticatedOwner;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/taxis")
public class TaxiController {

    private final TaxiService service;

    public TaxiController(TaxiService service) {
        this.service = service;
    }

    @GetMapping
    public List<TaxiResponse> list(@AuthenticationPrincipal AuthenticatedOwner owner,
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        return service.findAll(owner.id(), activeOnly);
    }

    @GetMapping("/{id}")
    public TaxiResponse get(@AuthenticationPrincipal AuthenticatedOwner owner, @PathVariable Long id) {
        return service.findById(owner.id(), id);
    }

    @PostMapping
    public ResponseEntity<TaxiResponse> create(@AuthenticationPrincipal AuthenticatedOwner owner,
            @Valid @RequestBody TaxiRequest request) {
        TaxiResponse created = service.create(owner.id(), request);
        return ResponseEntity.created(URI.create("/api/taxis/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public TaxiResponse update(@AuthenticationPrincipal AuthenticatedOwner owner, @PathVariable Long id,
            @Valid @RequestBody TaxiRequest request) {
        return service.update(owner.id(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@AuthenticationPrincipal AuthenticatedOwner owner, @PathVariable Long id) {
        service.deactivate(owner.id(), id);
        return ResponseEntity.noContent().build();
    }
}
