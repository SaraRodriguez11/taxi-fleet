package com.flota.taxis.driver;

public record DriverResponse(Long id, String fullName, String documentNumber, String phone, boolean active) {

    public static DriverResponse from(Driver driver) {
        return new DriverResponse(driver.getId(), driver.getFullName(), driver.getDocumentNumber(),
                driver.getPhone(), driver.isActive());
    }
}
