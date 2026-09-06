package com.tastyhouse.infrastructure.admin.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.admin.model.AdminRole;
import com.tastyhouse.domain.admin.model.AdminStatus;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "ADMIN")
public class AdminJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private AdminRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private AdminStatus status;

    protected AdminJpaEntity() {
    }

    private AdminJpaEntity(String username, String password, String name, AdminRole role, AdminStatus status) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role;
        this.status = status;
    }

    static AdminJpaEntity create(String username, String password, String name, AdminRole role, AdminStatus status) {
        return new AdminJpaEntity(username, password, name, role, status);
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

    public AdminRole getRole() {
        return this.role;
    }

    public AdminStatus getStatus() {
        return this.status;
    }
}
