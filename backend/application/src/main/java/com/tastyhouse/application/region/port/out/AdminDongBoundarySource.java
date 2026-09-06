package com.tastyhouse.application.region.port.out;

import java.math.BigDecimal;
import java.util.List;

import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoRing;

public record AdminDongBoundarySource(
    String code,
    String sidoName,
    String sigunguName,
    String dongName,
    GeoPoint center,
    List<GeoRing> boundary
) {

    public AdminDongBoundarySource {
        boundary = boundary == null ? List.of() : List.copyOf(boundary);
    }

    public static AdminDongBoundarySource of(
        String code,
        String sidoName,
        String sigunguName,
        String dongName,
        BigDecimal centerLatitude,
        BigDecimal centerLongitude,
        List<GeoRing> boundary
    ) {
        return new AdminDongBoundarySource(
            code,
            sidoName,
            sigunguName,
            dongName,
            GeoPoint.of(centerLatitude, centerLongitude),
            boundary
        );
    }
}
