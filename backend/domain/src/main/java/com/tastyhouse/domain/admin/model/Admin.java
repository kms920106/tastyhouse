package com.tastyhouse.domain.admin.model;

import com.tastyhouse.domain.admin.vo.AdminId;

public class Admin {
    private final Long id;
    private final String username;
    private final String password;
    private final String name;
    private final AdminRole role;
    private final AdminStatus status;

    private Admin(Long id, String username, String password, String name, AdminRole role, AdminStatus status) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role;
        this.status = status;
    }

    public static Admin create(String username, String encodedPassword, String name, AdminRole role) {
        return new Admin(null, username, encodedPassword, name, role, AdminStatus.ACTIVE);
    }

    public static Admin reconstitute(
        Long id,
        String username,
        String password,
        String name,
        AdminRole role,
        AdminStatus status
    ) {
        return new Admin(id, username, password, name, role, status);
    }

    public AdminId getAdminId() {
        return AdminId.of(this.id);
    }

    public boolean isActive() {
        return this.status == AdminStatus.ACTIVE;
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
