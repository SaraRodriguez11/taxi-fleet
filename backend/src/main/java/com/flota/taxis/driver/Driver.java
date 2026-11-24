package com.flota.taxis.driver;

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
@Table(name = "drivers")
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "driver_seq")
    @SequenceGenerator(name = "driver_seq", sequenceName = "driver_seq", allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false, updatable = false)
    private AppUser owner;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(name = "document_number", nullable = false, length = 20)
    private String documentNumber;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    private boolean active = true;

    protected Driver() {
        // requerido por JPA
    }

    public Driver(AppUser owner, String fullName, String documentNumber, String phone) {
        this.owner = owner;
        update(fullName, documentNumber, phone);
    }

    public void update(String fullName, String documentNumber, String phone) {
        this.fullName = fullName.trim();
        this.documentNumber = documentNumber.trim();
        this.phone = phone == null || phone.isBlank() ? null : phone.trim();
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

    public String getFullName() {
        return fullName;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isActive() {
        return active;
    }
}
