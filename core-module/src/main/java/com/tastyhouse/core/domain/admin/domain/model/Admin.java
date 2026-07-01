package com.tastyhouse.core.domain.admin.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.shared.entity.BaseEntity;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "ADMIN")
public class Admin extends BaseEntity {

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
    private AdminStatus status = AdminStatus.ACTIVE;

    private Admin(String username, String password, String name, AdminRole role) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    /**
     * 관리자 계정 생성. password는 이미 인코딩된 값이어야 한다.
     */
    public static Admin create(String username, String encodedPassword, String name, AdminRole role) {
        return new Admin(username, encodedPassword, name, role);
    }

    public boolean isActive() {
        return this.status == AdminStatus.ACTIVE;
    }
}
