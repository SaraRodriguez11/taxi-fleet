package com.flota.taxis.taxi;

import com.flota.taxis.driver.Driver;

public record TaxiResponse(Long id, String plate, String model, boolean active, Long driverId, String driverName) {

    public static TaxiResponse from(Taxi taxi) {
        Driver driver = taxi.getDriver();
        return new TaxiResponse(taxi.getId(), taxi.getPlate(), taxi.getModel(), taxi.isActive(),
                driver == null ? null : driver.getId(),
                driver == null ? null : driver.getFullName());
    }
}
