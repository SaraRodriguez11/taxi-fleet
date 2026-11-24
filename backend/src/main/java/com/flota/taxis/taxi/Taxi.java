package com.flota.taxis.taxi;

import java.util.Locale;

import com.flota.taxis.driver.Driver;
import com.flota.taxis.user.AppUser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "taxis")
public class Taxi {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "taxi_seq")
    @SequenceGenerator(name = "taxi_seq", sequenceName = "taxi_seq", allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false, updatable = false)
    private AppUser owner;

    @Column(nullable = false, length = 6, unique = true)
    private String plate;

    @Column(nullable = false, length = 80)
    private String model;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    protected Taxi() {
        // requerido por JPA
    }

    public Taxi(AppUser owner, String plate, String model, Driver driver) {
        this.owner = owner;
        update(plate, model, driver);
    }

    public void update(String plate, String model, Driver driver) {
        this.plate = plate.trim().toUpperCase(Locale.ROOT);
        this.model = model.trim();
        this.driver = driver;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public AppUser getOwner() {
        return owner;
    }

    public String getPlate() {
        return plate;
    }

    public String getModel() {
        return model;
    }

    public boolean isActive() {
        return active;
    }

    public Driver getDriver() {
        return driver;
    }
}
