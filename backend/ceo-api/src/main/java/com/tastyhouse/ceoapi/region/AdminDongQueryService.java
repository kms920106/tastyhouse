package com.tastyhouse.ceoapi.region;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.region.query.AdminDongItemResult;
import com.tastyhouse.infrastructure.region.query.AdminDongQueryDao;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.ceoapi.region.response.AdminDongItemResponse;

/**
 * 행정동 검색 서비스(CQRS query 측).
 *
 * <p>배달가능지역·지역별 배달팁 설정 화면에서 행정동을 선택할 수 있게 하는 조회 전용 도메인이라
 * CommandService를 두지 않는다({@code ADMIN_DONG}은 시드 SQL로만 관리하는 read-only 마스터).
 */
@Service
@Transactional(readOnly = true)
public class AdminDongQueryService {

    private final AdminDongQueryDao adminDongQueryDao;

    public AdminDongQueryService(AdminDongQueryDao adminDongQueryDao) {
        this.adminDongQueryDao = adminDongQueryDao;
    }

    public PaginationResponse<AdminDongItemResponse> getAdminDongs(String keyword, int page, int size) {
        PageResult<AdminDongItemResult> pageResult = adminDongQueryDao.findAdminDongPage(keyword, PageQuery.of(page, size));

        return PaginationResponse.from(pageResult.map(this::toAdminDongItemResponse));
    }

    private AdminDongItemResponse toAdminDongItemResponse(AdminDongItemResult dto) {
        return AdminDongItemResponse.from(
            dto.id(),
            dto.code(),
            dto.regionName()
        );
    }
}
