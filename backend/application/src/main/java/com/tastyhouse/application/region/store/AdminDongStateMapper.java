package com.tastyhouse.application.region.store;

import java.util.List;

import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.shared.geo.GeoBoundingBox;
import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoPolygonTextCodec;
import com.tastyhouse.domain.shared.geo.GeoRing;
import com.tastyhouse.application.region.port.out.write.AdminDongBoundarySnapshot;
import com.tastyhouse.application.region.port.out.write.AdminDongCenterSnapshot;
import com.tastyhouse.application.region.port.out.write.AdminDongState;

final class AdminDongStateMapper {
    private AdminDongStateMapper() {
    }

    static AdminDong toDomain(AdminDongState state) {
        return AdminDong.reconstitute(
            state.id(),
            state.code(),
            state.sidoName(),
            state.sigunguName(),
            state.dongName(),
            state.active(),
            toCenter(state.center()),
            GeoPolygonTextCodec.decodeRings(state.boundary() == null ? null : state.boundary().encodedRings())
        );
    }

    static AdminDongState toState(AdminDong adminDong) {
        return new AdminDongState(
            adminDong.getId(),
            adminDong.getCode(),
            adminDong.getSidoName(),
            adminDong.getSigunguName(),
            adminDong.getDongName(),
            adminDong.isActive(),
            toCenterSnapshot(adminDong.getCenter()),
            toBoundarySnapshot(adminDong.getBoundary())
        );
    }

    private static GeoPoint toCenter(AdminDongCenterSnapshot center) {
        if (center == null || center.latitude() == null || center.longitude() == null) {
            return null;
        }
        return GeoPoint.of(center.latitude(), center.longitude());
    }

    private static AdminDongCenterSnapshot toCenterSnapshot(GeoPoint center) {
        return center == null ? null : new AdminDongCenterSnapshot(center.latitude(), center.longitude());
    }

    private static AdminDongBoundarySnapshot toBoundarySnapshot(List<GeoRing> boundary) {
        if (boundary.isEmpty()) {
            return null;
        }

        List<GeoPoint> points = boundary.stream().flatMap(ring -> ring.points().stream()).toList();
        GeoBoundingBox boundingBox = GeoBoundingBox.enclosing(points);
        return new AdminDongBoundarySnapshot(
            GeoPolygonTextCodec.encodeRings(boundary),
            boundingBox.minLatitude(),
            boundingBox.maxLatitude(),
            boundingBox.minLongitude(),
            boundingBox.maxLongitude()
        );
    }
}
