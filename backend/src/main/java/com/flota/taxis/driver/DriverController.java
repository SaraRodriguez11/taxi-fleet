package com.flota.taxis.driver;

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
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverService service;

    public DriverController(DriverService service) {
        this.service = service;
    }

    @GetMapping
    public List<DriverResponse> list(@AuthenticationPrincipal AuthenticatedOwner owner,
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        return service.findAll(owner.id(), activeOnly);
    }

    @GetMapping("/{id}")
    public DriverResponse get(@AuthenticationPrincipal AuthenticatedOwner owner, @PathVariable Long id) {
        return service.findById(owner.id(), id);
    }

    @PostMapping
    public ResponseEntity<DriverResponse> create(@AuthenticationPrincipal AuthenticatedOwner owner,
            @Valid @RequestBody DriverRequest request) {
        DriverResponse created = service.create(owner.id(), request);
        return ResponseEntity.created(URI.create("/api/drivers/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public DriverResponse update(@AuthenticationPrincipal AuthenticatedOwner owner, @PathVariable Long id,
            @Valid @RequestBody DriverRequest request) {
        return service.update(owner.id(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@AuthenticationPrincipal AuthenticatedOwner owner, @PathVariable Long id) {
        service.deactivate(owner.id(), id);
        return ResponseEntity.noContent().build();
    }
}
