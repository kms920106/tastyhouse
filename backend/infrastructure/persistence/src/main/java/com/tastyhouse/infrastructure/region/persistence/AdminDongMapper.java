package com.tastyhouse.infrastructure.region.persistence;

import java.util.List;

import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.shared.geo.GeoBoundingBox;
import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoRing;
import com.tastyhouse.infrastructure.shared.persistence.GeoPolygonTextCodec;

final class AdminDongMapper {
    private AdminDongMapper() {
    }

    static AdminDongJpaEntity toEntity(AdminDong adminDong) {
        GeoPoint center = adminDong.getCenter();
        return AdminDongJpaEntity.create(
            adminDong.getCode(),
            adminDong.getSidoName(),
            adminDong.getSigunguName(),
            adminDong.getDongName(),
            adminDong.isActive(),
            center == null ? null : center.latitude(),
            center == null ? null : center.longitude(),
            toBoundingBox(adminDong.getBoundary()),
            GeoPolygonTextCodec.encodeRings(adminDong.getBoundary())
        );
    }

    static void applyChanges(AdminDongJpaEntity entity, AdminDong adminDong) {
        GeoPoint center = adminDong.getCenter();
        entity.applyChanges(
            adminDong.getSidoName(),
            adminDong.getSigunguName(),
            adminDong.getDongName(),
            adminDong.isActive(),
            center == null ? null : center.latitude(),
            center == null ? null : center.longitude(),
            toBoundingBox(adminDong.getBoundary()),
            GeoPolygonTextCodec.encodeRings(adminDong.getBoundary())
        );
    }

    private static GeoBoundingBox toBoundingBox(List<GeoRing> boundary) {
        if (boundary.isEmpty()) {
            return null;
        }

        List<GeoPoint> points = boundary.stream().flatMap(ring -> ring.points().stream()).toList();
        return GeoBoundingBox.enclosing(points);
    }

    static AdminDong toDomain(AdminDongJpaEntity entity) {
        return AdminDong.reconstitute(
            entity.getId(),
            entity.getCode(),
            entity.getSidoName(),
            entity.getSigunguName(),
            entity.getDongName(),
            entity.isActive(),
            toCenter(entity),
            GeoPolygonTextCodec.decodeRings(entity.getBoundary())
        );
    }

    private static GeoPoint toCenter(AdminDongJpaEntity entity) {
        if (entity.getCenterLatitude() == null || entity.getCenterLongitude() == null) {
            return null;
        }
        return GeoPoint.of(entity.getCenterLatitude(), entity.getCenterLongitude());
    }
}
