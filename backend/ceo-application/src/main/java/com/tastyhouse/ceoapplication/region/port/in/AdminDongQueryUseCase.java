package com.tastyhouse.ceoapplication.region.port.in;

import java.math.BigDecimal;
import java.util.List;

import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.ceoapplication.region.response.AdminDongBoundaryResponse;
import com.tastyhouse.ceoapplication.region.response.AdminDongItemResponse;
import com.tastyhouse.ceoapplication.region.response.AdminDongTreeResponse;

/**
 * 행정동 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code AdminDongQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface AdminDongQueryUseCase {

    PaginationResponse<AdminDongItemResponse> getAdminDongs(String keyword, int page, int size);

    AdminDongTreeResponse getAdminDongTree(String sidoName, String sigunguName);

    AdminDongBoundaryResponse getAdminDongBoundaries(
        BigDecimal swLat,
        BigDecimal swLng,
        BigDecimal neLat,
        BigDecimal neLng,
        Integer level,
        List<Long> adminDongIds
    );
}
