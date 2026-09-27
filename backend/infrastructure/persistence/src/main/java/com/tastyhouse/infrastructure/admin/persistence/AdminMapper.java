package com.tastyhouse.infrastructure.admin.persistence;

import com.tastyhouse.application.admin.port.out.write.AdminState;

final class AdminMapper {
    private AdminMapper() {
    }

    static AdminState toState(AdminJpaEntity entity) {
        return new AdminState(
            entity.getId(),
            entity.getUsername(),
            entity.getPassword(),
            entity.getName(),
            entity.getRole(),
            entity.getStatus()
        );
    }

    static AdminJpaEntity toEntity(AdminState state) {
        return AdminJpaEntity.create(
            state.username(),
            state.password(),
            state.name(),
            state.role(),
            state.status()
        );
    }
}
