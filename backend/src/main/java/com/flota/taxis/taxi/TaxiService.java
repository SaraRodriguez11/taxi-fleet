package com.flota.taxis.taxi;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flota.taxis.common.exception.BusinessRuleException;
import com.flota.taxis.common.exception.DuplicateResourceException;
import com.flota.taxis.common.exception.ResourceNotFoundException;
import com.flota.taxis.driver.Driver;
import com.flota.taxis.driver.DriverRepository;
import com.flota.taxis.user.UserRepository;

@Service
@Transactional(readOnly = true)
public class TaxiService {

    private final TaxiRepository taxis;
    private final DriverRepository drivers;
    private final UserRepository users;

    public TaxiService(TaxiRepository taxis, DriverRepository drivers, UserRepository users) {
        this.taxis = taxis;
        this.drivers = drivers;
        this.users = users;
    }

    public List<TaxiResponse> findAll(Long ownerId, boolean activeOnly) {
        List<Taxi> result = activeOnly
                ? taxis.findByOwnerIdAndActiveTrueOrderByPlateAsc(ownerId)
                : taxis.findByOwnerIdOrderByPlateAsc(ownerId);
        return result.stream().map(TaxiResponse::from).toList();
    }

    public TaxiResponse findById(Long ownerId, Long id) {
        return TaxiResponse.from(load(ownerId, id));
    }

    @Transactional
    public TaxiResponse create(Long ownerId, TaxiRequest request) {
        String plate = normalize(request.plate());
        if (taxis.existsByPlate(plate)) {
            throw new DuplicateResourceException("Ya existe un taxi con la placa " + plate);
        }
        Taxi taxi = new Taxi(users.getReferenceById(ownerId), plate, request.model(),
                resolveDriver(ownerId, request.driverId()));
        if (Boolean.FALSE.equals(request.active())) {
            taxi.deactivate();
        }
        return TaxiResponse.from(taxis.save(taxi));
    }

    @Transactional
    public TaxiResponse update(Long ownerId, Long id, TaxiRequest request) {
        Taxi taxi = load(ownerId, id);
        String plate = normalize(request.plate());
        if (taxis.existsByPlateAndIdNot(plate, id)) {
            throw new DuplicateResourceException("Ya existe un taxi con la placa " + plate);
        }
        taxi.update(plate, request.model(), resolveDriver(ownerId, request.driverId()));
        if (request.active() != null) {
            if (request.active()) {
                taxi.activate();
            } else {
                taxi.deactivate();
            }
        }
        return TaxiResponse.from(taxi);
    }

    /** Baja lógica: se conserva el historial contable. */
    @Transactional
    public void deactivate(Long ownerId, Long id) {
        load(ownerId, id).deactivate();
    }

    /** Solo se pueden asignar conductores del mismo propietario. */
    private Driver resolveDriver(Long ownerId, Long driverId) {
        if (driverId == null) {
            return null;
        }
        Driver driver = drivers.findByIdAndOwnerId(driverId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Conductor", driverId));
        if (!driver.isActive()) {
            throw new BusinessRuleException("No se puede asignar un conductor inactivo");
        }
        return driver;
    }

    private Taxi load(Long ownerId, Long id) {
        return taxis.findByIdAndOwnerId(id, ownerId).orElseThrow(() -> new ResourceNotFoundException("Taxi", id));
    }

    private static String normalize(String plate) {
        return plate.trim().toUpperCase(Locale.ROOT);
    }
}
