package com.flota.taxis.driver;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flota.taxis.common.exception.DuplicateResourceException;
import com.flota.taxis.common.exception.ResourceNotFoundException;
import com.flota.taxis.user.UserRepository;

@Service
@Transactional(readOnly = true)
public class DriverService {

    private final DriverRepository drivers;
    private final UserRepository users;

    public DriverService(DriverRepository drivers, UserRepository users) {
        this.drivers = drivers;
        this.users = users;
    }

    public List<DriverResponse> findAll(Long ownerId, boolean activeOnly) {
        List<Driver> result = activeOnly
                ? drivers.findByOwnerIdAndActiveTrueOrderByFullNameAsc(ownerId)
                : drivers.findByOwnerIdOrderByFullNameAsc(ownerId);
        return result.stream().map(DriverResponse::from).toList();
    }

    public DriverResponse findById(Long ownerId, Long id) {
        return DriverResponse.from(load(ownerId, id));
    }

    @Transactional
    public DriverResponse create(Long ownerId, DriverRequest request) {
        String document = request.documentNumber().trim();
        if (drivers.existsByOwnerIdAndDocumentNumber(ownerId, document)) {
            throw new DuplicateResourceException("Ya tienes un conductor con el documento " + document);
        }
        Driver driver = new Driver(users.getReferenceById(ownerId), request.fullName(), document, request.phone());
        if (Boolean.FALSE.equals(request.active())) {
            driver.deactivate();
        }
        return DriverResponse.from(drivers.save(driver));
    }

    @Transactional
    public DriverResponse update(Long ownerId, Long id, DriverRequest request) {
        Driver driver = load(ownerId, id);
        String document = request.documentNumber().trim();
        if (drivers.existsByOwnerIdAndDocumentNumberAndIdNot(ownerId, document, id)) {
            throw new DuplicateResourceException("Ya tienes un conductor con el documento " + document);
        }
        driver.update(request.fullName(), document, request.phone());
        if (request.active() != null) {
            if (request.active()) {
                driver.activate();
            } else {
                driver.deactivate();
            }
        }
        return DriverResponse.from(driver);
    }

    /** Baja lógica: se conserva el historial contable. */
    @Transactional
    public void deactivate(Long ownerId, Long id) {
        load(ownerId, id).deactivate();
    }

    private Driver load(Long ownerId, Long id) {
        return drivers.findByIdAndOwnerId(id, ownerId).orElseThrow(() -> new ResourceNotFoundException("Conductor", id));
    }
}
