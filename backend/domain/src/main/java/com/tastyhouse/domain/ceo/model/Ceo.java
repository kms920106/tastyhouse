package com.tastyhouse.domain.ceo.model;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.shared.vo.PhoneNumber;

public class Ceo {
    private final Long id;
    private final String username;
    private final String password;
    private final String name;
    private final String businessRegistrationNumber;
    private final PhoneNumber phoneNumber;
    private final String email;
    private final CeoStatus status;

    private Ceo(
        Long id,
        String username,
        String password,
        String name,
        String businessRegistrationNumber,
        PhoneNumber phoneNumber,
        String email,
        CeoStatus status
    ) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.businessRegistrationNumber = businessRegistrationNumber;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.status = status;
    }

    public static Ceo create(String username, String encodedPassword, String name) {
        return new Ceo(null, username, encodedPassword, name, null, null, null, CeoStatus.ACTIVE);
    }

    public static Ceo reconstitute(
        Long id,
        String username,
        String password,
        String name,
        String businessRegistrationNumber,
        PhoneNumber phoneNumber,
        String email,
        CeoStatus status
    ) {
        return new Ceo(id, username, password, name, businessRegistrationNumber, phoneNumber, email, status);
    }

    public CeoId getCeoId() {
        return CeoId.of(this.id);
    }

    public boolean isActive() {
        return this.status == CeoStatus.ACTIVE;
    }

    public Long getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getName() {
        return this.name;
    }

    public String getBusinessRegistrationNumber() {
        return this.businessRegistrationNumber;
    }

    public PhoneNumber getPhoneNumber() {
        return this.phoneNumber;
    }

    public String getEmail() {
        return this.email;
    }

    public CeoStatus getStatus() {
        return this.status;
    }
}
