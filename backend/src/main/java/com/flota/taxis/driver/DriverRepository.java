package com.flota.taxis.driver;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/** Todas las consultas filtran por propietario: un propietario nunca ve los conductores de otro. */
public interface DriverRepository extends JpaRepository<Driver, Long> {

    boolean existsByOwnerIdAndDocumentNumber(Long ownerId, String documentNumber);

    boolean existsByOwnerIdAndDocumentNumberAndIdNot(Long ownerId, String documentNumber, Long id);

    List<Driver> findByOwnerIdOrderByFullNameAsc(Long ownerId);

    List<Driver> findByOwnerIdAndActiveTrueOrderByFullNameAsc(Long ownerId);

    Optional<Driver> findByIdAndOwnerId(Long id, Long ownerId);

    long countByOwnerIdAndActiveTrue(Long ownerId);
}
