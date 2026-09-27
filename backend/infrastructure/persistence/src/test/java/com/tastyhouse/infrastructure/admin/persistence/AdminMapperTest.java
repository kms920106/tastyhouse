package com.tastyhouse.infrastructure.admin.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.admin.model.Admin;
import com.tastyhouse.domain.admin.model.AdminRole;
import com.tastyhouse.domain.admin.model.AdminStatus;

import static org.assertj.core.api.Assertions.assertThat;

class AdminMapperTest {

    @Test
    @DisplayName("Admin → 엔티티 변환 시 모든 컬럼 값이 옮겨진다")
    void toEntityCopiesColumns() {
        Admin original = Admin.reconstitute(3L, "admin-id", "encoded-pw", "관리자", AdminRole.SUPER_ADMIN, AdminStatus.INACTIVE);

        AdminJpaEntity entity = AdminMapper.toEntity(original);

        assertThat(entity.getUsername()).isEqualTo("admin-id");
        assertThat(entity.getPassword()).isEqualTo("encoded-pw");
        assertThat(entity.getName()).isEqualTo("관리자");
        assertThat(entity.getRole()).isEqualTo("SUPER_ADMIN");
        assertThat(entity.getStatus()).isEqualTo("INACTIVE");
    }

    @Test
    @DisplayName("엔티티 → Admin 변환 시 id를 포함한 모든 필드가 복원된다")
    void toDomainRestoresAllFields() {
        Admin original = Admin.reconstitute(3L, "admin-id", "encoded-pw", "관리자", AdminRole.SUPER_ADMIN, AdminStatus.INACTIVE);
        AdminJpaEntity entity = AdminMapper.toEntity(original);
        ReflectionTestUtils.setField(entity, "id", 3L);

        Admin restored = AdminMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("enum 필드는 상수명 문자열로 강등된다")
    void enumsAreStoredAsNames() {
        Admin original = Admin.reconstitute(4L, "u", "p", "n", AdminRole.ADMIN, AdminStatus.ACTIVE);

        AdminJpaEntity entity = AdminMapper.toEntity(original);

        assertThat(entity.getRole()).isEqualTo("ADMIN");
        assertThat(entity.getStatus()).isEqualTo("ACTIVE");
    }
}
