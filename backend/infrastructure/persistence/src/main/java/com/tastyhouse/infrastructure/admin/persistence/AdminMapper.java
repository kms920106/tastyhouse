package com.tastyhouse.infrastructure.admin.persistence;

import com.tastyhouse.domain.admin.model.Admin;

final class AdminMapper {
    private AdminMapper() {
    }

    static Admin toDomain(AdminJpaEntity entity) {
        return Admin.reconstitute(
            entity.getId(),
            entity.getUsername(),
            entity.getPassword(),
            entity.getName(),
            entity.getRole(),
            entity.getStatus()
        );
    }

    static AdminJpaEntity toEntity(Admin domain) {
        return AdminJpaEntity.create(
            domain.getUsername(),
            domain.getPassword(),
            domain.getName(),
            domain.getRole(),
            domain.getStatus()
        );
    }
}
