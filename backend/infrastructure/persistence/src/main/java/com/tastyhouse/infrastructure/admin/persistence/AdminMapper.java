package com.tastyhouse.infrastructure.admin.persistence;

import com.tastyhouse.domain.admin.model.Admin;
import com.tastyhouse.domain.admin.model.AdminRole;
import com.tastyhouse.domain.admin.model.AdminStatus;

final class AdminMapper {
    private AdminMapper() {
    }

    static Admin toDomain(AdminJpaEntity entity) {
        return Admin.reconstitute(
            entity.getId(),
            entity.getUsername(),
            entity.getPassword(),
            entity.getName(),
            entity.getRole() == null ? null : AdminRole.valueOf(entity.getRole()),
            entity.getStatus() == null ? null : AdminStatus.valueOf(entity.getStatus())
        );
    }

    static AdminJpaEntity toEntity(Admin admin) {
        return AdminJpaEntity.create(
            admin.getUsername(),
            admin.getPassword(),
            admin.getName(),
            admin.getRole() == null ? null : admin.getRole().name(),
            admin.getStatus() == null ? null : admin.getStatus().name()
        );
    }
}
