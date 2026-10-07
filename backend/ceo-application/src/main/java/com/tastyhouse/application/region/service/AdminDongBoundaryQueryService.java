package com.tastyhouse.application.region.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.geo.GeoPolygonTextCodec;
import com.tastyhouse.domain.shared.geo.GeoRing;
import com.tastyhouse.application.region.port.in.AdminDongBoundaryQueryUseCase;
import com.tastyhouse.application.region.port.out.AdminDongBoundariesResult;
import com.tastyhouse.application.region.port.out.AdminDongBoundaryResult;
import com.tastyhouse.application.region.port.out.AdminDongBoundaryViewResult;
import com.tastyhouse.application.region.port.out.AdminDongQueryPort;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.CeoErrorCode;

@Service
@Transactional(readOnly = true)
class AdminDongBoundaryQueryService implements AdminDongBoundaryQueryUseCase {

    private static final BigDecimal MAX_BOUNDARY_BOX_AREA_DEGREES = new BigDecimal("0.25");

    private static final int MAX_BOUNDARY_ITEMS = 200;

    private final AdminDongQueryPort adminDongQueryPort;

    public AdminDongBoundaryQueryService(AdminDongQueryPort adminDongQueryPort) {
        this.adminDongQueryPort = adminDongQueryPort;
    }

    @Override
    public AdminDongBoundariesResult getAdminDongBoundaries(
        BigDecimal swLat,
        BigDecimal swLng,
        BigDecimal neLat,
        BigDecimal neLng,
        Integer level,
        List<Long> adminDongIds
    ) {
        boolean hasIds = adminDongIds != null && !adminDongIds.isEmpty();
        boolean hasBoundingBox = swLat != null && swLng != null && neLat != null && neLng != null;

        if (hasIds && hasBoundingBox) {
            throw new ApplicationException(
                CeoErrorCode.ADMIN_DONG_QUERY_INVALID,
                "조회 영역(bbox)과 행정동 ID는 함께 지정할 수 없습니다."
            );
        }
        if (!hasIds && !hasBoundingBox) {
            throw new ApplicationException(
                CeoErrorCode.ADMIN_DONG_QUERY_INVALID,
                "조회 영역(bbox) 또는 행정동 ID 중 하나는 반드시 지정해야 합니다."
            );
        }

        if (hasIds) {
            return toAdminDongBoundariesResult(adminDongQueryPort.findBoundariesByIds(adminDongIds));
        }

        if (level == null) {
            throw new ApplicationException(CeoErrorCode.ADMIN_DONG_QUERY_INVALID, "조회 영역(bbox)에는 줌 레벨이 필요합니다.");
        }
        if (exceedsBoundingBoxLimit(swLat, swLng, neLat, neLng)) {
            return new AdminDongBoundariesResult(true, List.of());
        }

        return toAdminDongBoundariesResult(adminDongQueryPort.findBoundariesWithinBoundingBox(
            swLat, neLat, swLng, neLng, MAX_BOUNDARY_ITEMS
        ));
    }

    private boolean exceedsBoundingBoxLimit(BigDecimal swLat, BigDecimal swLng, BigDecimal neLat, BigDecimal neLng) {
        BigDecimal latitudeSpan = neLat.subtract(swLat).abs();
        BigDecimal longitudeSpan = neLng.subtract(swLng).abs();
        return latitudeSpan.multiply(longitudeSpan).compareTo(MAX_BOUNDARY_BOX_AREA_DEGREES) > 0;
    }

    private AdminDongBoundariesResult toAdminDongBoundariesResult(List<AdminDongBoundaryResult> items) {
        return new AdminDongBoundariesResult(
            false,
            items.stream().map(this::toBoundaryViewResult).toList()
        );
    }

    private AdminDongBoundaryViewResult toBoundaryViewResult(AdminDongBoundaryResult dto) {
        return new AdminDongBoundaryViewResult(
            dto.adminDongId(),
            dto.regionName(),
            dto.centerLatitude(),
            dto.centerLongitude(),
            toRings(dto.boundary())
        );
    }

    private List<List<AdminDongBoundaryViewResult.Point>> toRings(String boundary) {
        List<GeoRing> rings = GeoPolygonTextCodec.decodeRings(boundary);
        if (rings.isEmpty()) {
            return null;
        }

        return rings.stream()
            .map(ring -> ring.points().stream()
                .map(point -> new AdminDongBoundaryViewResult.Point(point.latitude(), point.longitude()))
                .toList())
            .toList();
    }
}
