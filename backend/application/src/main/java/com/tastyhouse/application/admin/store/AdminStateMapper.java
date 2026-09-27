package com.tastyhouse.application.admin.store;

import com.tastyhouse.application.admin.port.out.write.AdminState;
import com.tastyhouse.domain.admin.model.Admin;
import com.tastyhouse.domain.admin.model.AdminRole;
import com.tastyhouse.domain.admin.model.AdminStatus;

final class AdminStateMapper {
    private AdminStateMapper() {
    }

    static Admin toDomain(AdminState state) {
        return Admin.reconstitute(
            state.id(),
            state.username(),
            state.password(),
            state.name(),
            state.role() == null ? null : AdminRole.valueOf(state.role()),
            state.status() == null ? null : AdminStatus.valueOf(state.status())
        );
    }

    static AdminState toState(Admin admin) {
        return new AdminState(
            admin.getId(),
            admin.getUsername(),
            admin.getPassword(),
            admin.getName(),
            admin.getRole() == null ? null : admin.getRole().name(),
            admin.getStatus() == null ? null : admin.getStatus().name()
        );
    }
}
