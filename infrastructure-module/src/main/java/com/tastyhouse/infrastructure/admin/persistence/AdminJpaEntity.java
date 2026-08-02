package com.tastyhouse.infrastructure.admin.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.admin.domain.model.AdminRole;
import com.tastyhouse.domain.admin.domain.model.AdminStatus;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 관리자 계정 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code Admin}과 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code AdminMapper}가 수행한다.
 */
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

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code AdminMapper#toEntity}에서만 호출한다.
     */
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
