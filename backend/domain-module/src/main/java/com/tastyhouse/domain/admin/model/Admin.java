package com.tastyhouse.domain.admin.model;

import com.tastyhouse.domain.admin.vo.AdminId;

/**
 * 관리자 계정 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code AdminJpaEntity} + {@code AdminMapper}가 담당한다. 도메인이 프레임워크-프리이므로
 * 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로 {@code AdminRepository#save}를
 * 호출해야 한다.
 */
public class Admin {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
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

    /**
     * 신규 관리자 계정을 생성한다. password는 이미 인코딩된 값이어야 한다.
     */
    public static Admin create(String username, String encodedPassword, String name, AdminRole role) {
        return new Admin(null, username, encodedPassword, name, role, AdminStatus.ACTIVE);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자를 주입한다.
     */
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
