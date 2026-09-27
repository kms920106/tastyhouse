package com.tastyhouse.application.admin.store;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.admin.model.Admin;
import com.tastyhouse.domain.admin.model.AdminRole;
import com.tastyhouse.domain.admin.model.AdminStatus;

import static org.assertj.core.api.Assertions.assertThat;

class AdminStateMapperTest {

    @Test
    @DisplayName("Admin → AdminState → Admin 왕복 시 모든 필드가 보존된다")
    void roundTrip() {
        Admin original = Admin.reconstitute(3L, "admin-id", "encoded-pw", "관리자", AdminRole.SUPER_ADMIN, AdminStatus.INACTIVE);

        Admin restored = AdminStateMapper.toDomain(AdminStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("enum 필드는 상수명 문자열로 강등된다")
    void enumsAreStoredAsNames() {
        Admin original = Admin.reconstitute(4L, "u", "p", "n", AdminRole.ADMIN, AdminStatus.ACTIVE);

        assertThat(AdminStateMapper.toState(original).role()).isEqualTo("ADMIN");
        assertThat(AdminStateMapper.toState(original).status()).isEqualTo("ACTIVE");
    }
}
