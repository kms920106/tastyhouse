package com.tastyhouse.domain.admin.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.admin.domain.vo.AdminId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스).
 */
class AdminTest {

    @Test
    @DisplayName("create로 생성하면 미영속 상태(식별자 없음)이고 상태는 ACTIVE다")
    void create_createsTransientActiveAdmin() {
        Admin admin = Admin.create("admin01", "encodedPassword", "관리자", AdminRole.ADMIN);

        assertThat(admin.getId()).isNull();
        assertThat(admin.getUsername()).isEqualTo("admin01");
        assertThat(admin.getPassword()).isEqualTo("encodedPassword");
        assertThat(admin.getName()).isEqualTo("관리자");
        assertThat(admin.getRole()).isEqualTo(AdminRole.ADMIN);
        assertThat(admin.getStatus()).isEqualTo(AdminStatus.ACTIVE);
        assertThat(admin.isActive()).isTrue();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·상태를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        Admin admin = Admin.reconstitute(
            1L, "admin01", "encodedPassword", "관리자", AdminRole.SUPER_ADMIN, AdminStatus.ACTIVE
        );

        assertThat(admin.getId()).isEqualTo(1L);
        assertThat(admin.getAdminId()).isEqualTo(AdminId.of(1L));
        assertThat(admin.getRole()).isEqualTo(AdminRole.SUPER_ADMIN);
        assertThat(admin.isActive()).isTrue();
    }

    @Test
    @DisplayName("status가 INACTIVE로 재구성되면 isActive는 false다")
    void reconstitute_inactiveStatus_isActiveFalse() {
        Admin admin = Admin.reconstitute(
            1L, "admin01", "encodedPassword", "관리자", AdminRole.ADMIN, AdminStatus.INACTIVE
        );

        assertThat(admin.isActive()).isFalse();
    }

    @Test
    @DisplayName("미영속 상태에서 getAdminId를 호출하면 AdminId 불변식 위반으로 예외가 발생한다")
    void getAdminId_onTransient_throws() {
        Admin admin = Admin.create("admin01", "encodedPassword", "관리자", AdminRole.ADMIN);

        assertThatThrownBy(admin::getAdminId)
            .isInstanceOf(IllegalArgumentException.class);
    }
}
