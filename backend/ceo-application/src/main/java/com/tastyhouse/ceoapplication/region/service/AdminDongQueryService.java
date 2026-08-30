package com.tastyhouse.ceoapplication.region.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tastyhouse.ceoapplication.region.port.in.AdminDongQueryUseCase;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.geo.GeoRing;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.region.port.out.AdminDongBoundaryResult;
import com.tastyhouse.application.region.port.out.AdminDongItemResult;
import com.tastyhouse.application.region.port.out.AdminDongQueryPort;
import com.tastyhouse.application.region.port.out.AdminDongTreeItemResult;
import com.tastyhouse.application.shared.port.out.GeoRingsPort;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.ceoapplication.region.response.AdminDongBoundaryItemResponse;
import com.tastyhouse.ceoapplication.region.response.AdminDongBoundaryResponse;
import com.tastyhouse.ceoapplication.region.response.AdminDongItemResponse;
import com.tastyhouse.ceoapplication.region.response.AdminDongPointResponse;
import com.tastyhouse.ceoapplication.region.response.AdminDongTreeItemResponse;
import com.tastyhouse.ceoapplication.region.response.AdminDongTreeResponse;

/**
 * 행정동 검색 서비스(CQRS query 측).
 *
 * <p>배달가능지역·지역별 배달팁 설정 화면에서 행정동을 선택할 수 있게 하는 조회 전용 도메인이라
 * CommandService를 두지 않는다({@code ADMIN_DONG}은 시드 SQL로만 관리하는 read-only 마스터).
 */
@Service
@Transactional(readOnly = true)
public class AdminDongQueryService implements AdminDongQueryUseCase {

    private static final String LEVEL_SIDO = "SIDO";
    private static final String LEVEL_SIGUNGU = "SIGUNGU";
    private static final String LEVEL_DONG = "DONG";

    /**
     * 경계를 내려보낼 최대 bbox 면적(제곱도). 약 0.5° × 0.5°(대략 55km × 44km, 광역시 하나 규모)에
     * 해당하며, 이보다 넓으면 경계를 생략하고 {@code truncated}로 알린다.
     */
    private static final BigDecimal MAX_BOUNDARY_BOX_AREA_DEGREES = new BigDecimal("0.25");

    /** 한 번의 bbox 조회로 내려보낼 최대 행정동 수. 임계 면적 안이라도 응답 크기를 최종적으로 제한한다. */
    private static final int MAX_BOUNDARY_ITEMS = 200;

    private final AdminDongQueryPort adminDongQueryPort;
    private final GeoRingsPort geoRingsPort;

    public AdminDongQueryService(AdminDongQueryPort adminDongQueryPort, GeoRingsPort geoRingsPort) {
        this.adminDongQueryPort = adminDongQueryPort;
        this.geoRingsPort = geoRingsPort;
    }

    @Override
    public PaginationResponse<AdminDongItemResponse> getAdminDongs(String keyword, int page, int size) {
        PageResult<AdminDongItemResult> pageResult = adminDongQueryPort.findAdminDongPage(keyword, PageQuery.of(page, size));

        return PaginationResponse.from(pageResult.map(this::toAdminDongItemResponse));
    }

    /**
     * 행정동 계층을 한 단계씩 조회한다(시도 → 시군구 → 행정동).
     *
     * <p>전 계층을 한 번에 내리지 않는 이유는 전국 행정동이 3,600건을 넘어, 대부분 화면에 쓰이지 않는
     * 데이터로 응답이 비대해지기 때문이다.
     *
     * <p>{@code sigunguName}만 단독으로 오면 400이다 — 같은 이름의 시군구가 여러 시도에 존재하므로
     * ("중구"는 서울·부산·대구 등에 있다) 상위 계층 없이는 대상을 특정할 수 없다.
     */
    @Override
    public AdminDongTreeResponse getAdminDongTree(String sidoName, String sigunguName) {
        boolean hasSido = StringUtils.hasText(sidoName);
        boolean hasSigungu = StringUtils.hasText(sigunguName);

        if (hasSigungu && !hasSido) {
            throw new BusinessException(
                ErrorCode.ADMIN_DONG_QUERY_INVALID,
                "시/군/구를 지정하려면 시/도도 함께 지정해야 합니다."
            );
        }

        if (!hasSido) {
            return toAdminDongTreeResponse(LEVEL_SIDO, adminDongQueryPort.findSidoNames());
        }
        if (!hasSigungu) {
            return toAdminDongTreeResponse(LEVEL_SIGUNGU, adminDongQueryPort.findSigunguNames(sidoName.trim()));
        }
        return toAdminDongTreeResponse(
            LEVEL_DONG,
            adminDongQueryPort.findDongs(sidoName.trim(), sigunguName.trim())
        );
    }

    /**
     * 지도 영역(bbox) 또는 식별자 목록으로 행정동 경계를 조회한다.
     *
     * <p>bbox가 임계 면적을 넘으면 <b>400이 아니라 빈 배열 + {@code truncated: true}</b>로 응답한다 —
     * 지도를 축소하는 것은 정상 조작이고, 그때마다 오류를 띄우면 화면이 쓸 수 없게 된다.
     */
    @Override
    public AdminDongBoundaryResponse getAdminDongBoundaries(
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
            return toAdminDongBoundaryResponse(adminDongQueryPort.findBoundariesByIds(adminDongIds));
        }

        if (level == null) {
            throw new BusinessException(ErrorCode.ADMIN_DONG_QUERY_INVALID, "조회 영역(bbox)에는 줌 레벨이 필요합니다.");
        }
        if (exceedsBoundingBoxLimit(swLat, swLng, neLat, neLng)) {
            return AdminDongBoundaryResponse.from(true, List.of());
        }

        return toAdminDongBoundaryResponse(adminDongQueryPort.findBoundariesWithinBoundingBox(
            swLat, neLat, swLng, neLng, MAX_BOUNDARY_ITEMS
        ));
    }

    /**
     * bbox가 경계를 내려보내기에 너무 넓은지 판정한다.
     *
     * <p>면적(위도폭 × 경도폭)으로 재는 이유는, 한 변만 긴 띠 모양 영역(고속도로를 따라 확대한 경우 등)은
     * 포함 행정동 수가 많지 않아 굳이 막을 필요가 없기 때문이다.
     */
    private boolean exceedsBoundingBoxLimit(BigDecimal swLat, BigDecimal swLng, BigDecimal neLat, BigDecimal neLng) {
        BigDecimal latitudeSpan = neLat.subtract(swLat).abs();
        BigDecimal longitudeSpan = neLng.subtract(swLng).abs();
        return latitudeSpan.multiply(longitudeSpan).compareTo(MAX_BOUNDARY_BOX_AREA_DEGREES) > 0;
    }

    private AdminDongTreeResponse toAdminDongTreeResponse(String level, List<AdminDongTreeItemResult> items) {
        return AdminDongTreeResponse.from(
            level,
            items.stream().map(this::toAdminDongTreeItemResponse).toList()
        );
    }

    private AdminDongTreeItemResponse toAdminDongTreeItemResponse(AdminDongTreeItemResult dto) {
        return AdminDongTreeItemResponse.from(
            dto.name(),
            dto.adminDongId(),
            dto.code(),
            dto.dongCount()
        );
    }

    /**
     * 조회된 경계를 응답으로 옮긴다. 여기로 오는 경로는 전부 잘리지 않은 결과이므로
     * {@code truncated}는 항상 {@code false}다 — 임계 면적을 넘긴 경우는 이 메서드를 타지 않고
     * 위에서 빈 배열 + {@code true}로 곧장 응답한다.
     */
    private AdminDongBoundaryResponse toAdminDongBoundaryResponse(List<AdminDongBoundaryResult> items) {
        return AdminDongBoundaryResponse.from(
            false,
            items.stream().map(this::toAdminDongBoundaryItemResponse).toList()
        );
    }

    private AdminDongBoundaryItemResponse toAdminDongBoundaryItemResponse(AdminDongBoundaryResult dto) {
        return AdminDongBoundaryItemResponse.from(
            dto.adminDongId(),
            dto.regionName(),
            dto.centerLatitude(),
            dto.centerLongitude(),
            toRings(dto.boundary())
        );
    }

    /**
     * 인코딩된 경계 문자열을 좌표 객체 배열로 푼다. 경계 미보유는 {@code null}로 남겨 "데이터가 없다"와
     * "경계가 빈 도형이다"를 구분한다.
     */
    private List<List<AdminDongPointResponse>> toRings(String boundary) {
        List<GeoRing> rings = geoRingsPort.resolveRings(boundary);
        if (rings.isEmpty()) {
            return null;
        }

        return rings.stream()
            .map(ring -> ring.points().stream()
                .map(point -> AdminDongPointResponse.from(point.latitude(), point.longitude()))
                .toList())
            .toList();
    }

    private AdminDongItemResponse toAdminDongItemResponse(AdminDongItemResult dto) {
        return AdminDongItemResponse.from(
            dto.id(),
            dto.code(),
            dto.regionName()
        );
    }
}
