package com.tastyhouse.infrastructure.region.persistence;

import com.tastyhouse.application.region.port.out.write.AdminDongBoundarySnapshot;
import com.tastyhouse.application.region.port.out.write.AdminDongCenterSnapshot;
import com.tastyhouse.application.region.port.out.write.AdminDongState;

final class AdminDongMapper {
    private AdminDongMapper() {
    }

    static AdminDongJpaEntity toEntity(AdminDongState state) {
        AdminDongCenterSnapshot center = state.center();
        AdminDongBoundarySnapshot boundary = state.boundary();
        return AdminDongJpaEntity.create(
            state.code(),
            state.sidoName(),
            state.sigunguName(),
            state.dongName(),
            state.active(),
            center == null ? null : center.latitude(),
            center == null ? null : center.longitude(),
            boundary == null ? null : boundary.encodedRings(),
            boundary == null ? null : boundary.minLatitude(),
            boundary == null ? null : boundary.maxLatitude(),
            boundary == null ? null : boundary.minLongitude(),
            boundary == null ? null : boundary.maxLongitude()
        );
    }

    static void applyChanges(AdminDongJpaEntity entity, AdminDongState state) {
        AdminDongCenterSnapshot center = state.center();
        AdminDongBoundarySnapshot boundary = state.boundary();
        entity.applyChanges(
            state.sidoName(),
            state.sigunguName(),
            state.dongName(),
            state.active(),
            center == null ? null : center.latitude(),
            center == null ? null : center.longitude(),
            boundary == null ? null : boundary.encodedRings(),
            boundary == null ? null : boundary.minLatitude(),
            boundary == null ? null : boundary.maxLatitude(),
            boundary == null ? null : boundary.minLongitude(),
            boundary == null ? null : boundary.maxLongitude()
        );
    }

    static AdminDongState toState(AdminDongJpaEntity entity) {
        return new AdminDongState(
            entity.getId(),
            entity.getCode(),
            entity.getSidoName(),
            entity.getSigunguName(),
            entity.getDongName(),
            entity.isActive(),
            toCenter(entity),
            toBoundary(entity)
        );
    }

    private static AdminDongCenterSnapshot toCenter(AdminDongJpaEntity entity) {
        if (entity.getCenterLatitude() == null || entity.getCenterLongitude() == null) {
            return null;
        }
        return new AdminDongCenterSnapshot(entity.getCenterLatitude(), entity.getCenterLongitude());
    }

    private static AdminDongBoundarySnapshot toBoundary(AdminDongJpaEntity entity) {
        if (entity.getBoundary() == null) {
            return null;
        }
        return new AdminDongBoundarySnapshot(
            entity.getBoundary(),
            entity.getBoundaryMinLatitude(),
            entity.getBoundaryMaxLatitude(),
            entity.getBoundaryMinLongitude(),
            entity.getBoundaryMaxLongitude()
        );
    }
}
