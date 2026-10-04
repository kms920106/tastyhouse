package com.tastyhouse.infrastructure.persistence.ceo.persistence;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.persistence.shared.persistence.BaseEntity;
import com.tastyhouse.infrastructure.persistence.shared.persistence.PhoneNumberEmbeddable;

@Entity
@Table(name = "CEO")
public class CeoJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "business_registration_number", length = 20)
    private String businessRegistrationNumber;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "phone_number", length = 11))
    private PhoneNumberEmbeddable phoneNumber;

    @Column(name = "email", length = 200)
    private String email;

    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private String status;

    protected CeoJpaEntity() {
    }

    private CeoJpaEntity(
        String username,
        String password,
        String name,
        String businessRegistrationNumber,
        PhoneNumberEmbeddable phoneNumber,
        String email,
        String status
    ) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.businessRegistrationNumber = businessRegistrationNumber;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.status = status;
    }

    static CeoJpaEntity create(
        String username,
        String password,
        String name,
        String businessRegistrationNumber,
        PhoneNumberEmbeddable phoneNumber,
        String email,
        String status
    ) {
        return new CeoJpaEntity(username, password, name, businessRegistrationNumber, phoneNumber, email, status);
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

    public PhoneNumberEmbeddable getPhoneNumber() {
        return this.phoneNumber;
    }

    public String getEmail() {
        return this.email;
    }

    public String getStatus() {
        return this.status;
    }
}
