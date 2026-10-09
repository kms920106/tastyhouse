package com.tastyhouse.infrastructure.jpa.region.persistence;

import java.util.List;

import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.shared.geo.GeoBoundingBox;
import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoPolygonTextCodec;
import com.tastyhouse.domain.shared.geo.GeoRing;

final class AdminDongMapper {

    private AdminDongMapper() {
    }

    static AdminDongJpaEntity toEntity(AdminDong adminDong) {
        GeoPoint center = adminDong.getCenter();
        List<GeoRing> boundary = adminDong.getBoundary();
        GeoBoundingBox boundingBox = enclosingBoundingBox(boundary);
        return AdminDongJpaEntity.create(
            adminDong.getCode(),
            adminDong.getSidoName(),
            adminDong.getSigunguName(),
            adminDong.getDongName(),
            adminDong.isActive(),
            center == null ? null : center.latitude(),
            center == null ? null : center.longitude(),
            boundingBox == null ? null : GeoPolygonTextCodec.encodeRings(boundary),
            boundingBox == null ? null : boundingBox.minLatitude(),
            boundingBox == null ? null : boundingBox.maxLatitude(),
            boundingBox == null ? null : boundingBox.minLongitude(),
            boundingBox == null ? null : boundingBox.maxLongitude()
        );
    }

    static void applyChanges(AdminDongJpaEntity entity, AdminDong adminDong) {
        GeoPoint center = adminDong.getCenter();
        List<GeoRing> boundary = adminDong.getBoundary();
        GeoBoundingBox boundingBox = enclosingBoundingBox(boundary);
        entity.applyChanges(
            adminDong.getSidoName(),
            adminDong.getSigunguName(),
            adminDong.getDongName(),
            adminDong.isActive(),
            center == null ? null : center.latitude(),
            center == null ? null : center.longitude(),
            boundingBox == null ? null : GeoPolygonTextCodec.encodeRings(boundary),
            boundingBox == null ? null : boundingBox.minLatitude(),
            boundingBox == null ? null : boundingBox.maxLatitude(),
            boundingBox == null ? null : boundingBox.minLongitude(),
            boundingBox == null ? null : boundingBox.maxLongitude()
        );
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

    private static GeoBoundingBox enclosingBoundingBox(List<GeoRing> boundary) {
        if (boundary.isEmpty()) {
            return null;
        }

        List<GeoPoint> points = boundary.stream().flatMap(ring -> ring.points().stream()).toList();
        return GeoBoundingBox.enclosing(points);
    }
}
