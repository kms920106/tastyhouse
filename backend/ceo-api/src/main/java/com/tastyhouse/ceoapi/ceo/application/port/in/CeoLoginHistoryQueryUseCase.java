package com.tastyhouse.ceoapi.ceo.application.port.in;

import java.time.LocalDate;

import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.ceoapi.ceo.adapter.in.web.response.CeoLoginHistoryListItemResponse;

/**
 * 점주 로그인 이력 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code CeoLoginHistoryQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface CeoLoginHistoryQueryUseCase {

    PaginationResponse<CeoLoginHistoryListItemResponse> getLoginHistories(
        Long ceoId,
        String result,
        LocalDate startDate,
        LocalDate endDate,
        int page,
        int size
    );
}
