package com.tastyhouse.application.region.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tastyhouse.application.region.port.in.AdminDongQueryUseCase;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.geo.GeoRing;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.region.port.out.AdminDongBoundariesResult;
import com.tastyhouse.application.region.port.out.AdminDongBoundaryResult;
import com.tastyhouse.application.region.port.out.AdminDongBoundaryViewResult;
import com.tastyhouse.application.region.port.out.AdminDongItemResult;
import com.tastyhouse.application.region.port.out.AdminDongQueryPort;
import com.tastyhouse.application.region.port.out.AdminDongTreeItemResult;
import com.tastyhouse.application.region.port.out.AdminDongTreeResult;
import com.tastyhouse.application.shared.port.out.GeoRingsQueryPort;

@Service
@CeoApp
@Transactional(readOnly = true)
public class AdminDongQueryService implements AdminDongQueryUseCase {

    private static final String LEVEL_SIDO = "SIDO";
    private static final String LEVEL_SIGUNGU = "SIGUNGU";
    private static final String LEVEL_DONG = "DONG";

    private static final BigDecimal MAX_BOUNDARY_BOX_AREA_DEGREES = new BigDecimal("0.25");

    private static final int MAX_BOUNDARY_ITEMS = 200;

    private final AdminDongQueryPort adminDongQueryPort;
    private final GeoRingsQueryPort geoRingsPort;

    public AdminDongQueryService(AdminDongQueryPort adminDongQueryPort, GeoRingsQueryPort geoRingsPort) {
        this.adminDongQueryPort = adminDongQueryPort;
        this.geoRingsPort = geoRingsPort;
    }

    @Override
    public PageResult<AdminDongItemResult> getAdminDongs(String keyword, int page, int size) {
        return adminDongQueryPort.findAdminDongPage(keyword, PageQuery.of(page, size));
    }

    @Override
    public AdminDongTreeResult getAdminDongTree(String sidoName, String sigunguName) {
        boolean hasSido = StringUtils.hasText(sidoName);
        boolean hasSigungu = StringUtils.hasText(sigunguName);

        if (hasSigungu && !hasSido) {
            throw new BusinessException(
                ErrorCode.ADMIN_DONG_QUERY_INVALID,
                "시/군/구를 지정하려면 시/도도 함께 지정해야 합니다."
            );
        }

        if (!hasSido) {
            return toAdminDongTreeResult(LEVEL_SIDO, adminDongQueryPort.findSidoNames());
        }
        if (!hasSigungu) {
            return toAdminDongTreeResult(LEVEL_SIGUNGU, adminDongQueryPort.findSigunguNames(sidoName.trim()));
        }
        return toAdminDongTreeResult(
            LEVEL_DONG,
            adminDongQueryPort.findDongs(sidoName.trim(), sigunguName.trim())
        );
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
            throw new BusinessException(
                ErrorCode.ADMIN_DONG_QUERY_INVALID,
                "조회 영역(bbox)과 행정동 ID는 함께 지정할 수 없습니다."
            );
        }
        if (!hasIds && !hasBoundingBox) {
            throw new BusinessException(
                ErrorCode.ADMIN_DONG_QUERY_INVALID,
                "조회 영역(bbox) 또는 행정동 ID 중 하나는 반드시 지정해야 합니다."
            );
        }

        if (hasIds) {
            return toAdminDongBoundariesResult(adminDongQueryPort.findBoundariesByIds(adminDongIds));
        }

        if (level == null) {
            throw new BusinessException(ErrorCode.ADMIN_DONG_QUERY_INVALID, "조회 영역(bbox)에는 줌 레벨이 필요합니다.");
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

    private AdminDongTreeResult toAdminDongTreeResult(String level, List<AdminDongTreeItemResult> items) {
        return new AdminDongTreeResult(level, items);
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
        List<GeoRing> rings = geoRingsPort.resolveRings(boundary);
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
