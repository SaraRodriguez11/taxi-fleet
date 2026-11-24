package com.flota.taxis.taxi;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/** Todas las consultas filtran por propietario: un propietario nunca ve los taxis de otro. */
public interface TaxiRepository extends JpaRepository<Taxi, Long> {

    /** La placa es única en todo el sistema (un taxi no se registra dos veces). */
    boolean existsByPlate(String plate);

    boolean existsByPlateAndIdNot(String plate, Long id);

    boolean existsByIdAndOwnerId(Long id, Long ownerId);

    long countByOwnerIdAndActiveTrue(Long ownerId);

    // EntityGraph: carga el conductor en la misma consulta (evita el problema N+1)
    @EntityGraph(attributePaths = "driver")
    List<Taxi> findByOwnerIdOrderByPlateAsc(Long ownerId);

    @EntityGraph(attributePaths = "driver")
    List<Taxi> findByOwnerIdAndActiveTrueOrderByPlateAsc(Long ownerId);

    @EntityGraph(attributePaths = "driver")
    Optional<Taxi> findByIdAndOwnerId(Long id, Long ownerId);
}
