package com.tastyhouse.adminapi.point.application.port.in;

import com.tastyhouse.adminapi.point.adapter.in.web.response.PointBalanceResponse;
import com.tastyhouse.adminapi.point.adapter.in.web.response.PointHistoryResponse;
import com.tastyhouse.apicommon.common.PaginationResponse;

/**
 * 포인트 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code PointQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface PointQueryUseCase {

    PointBalanceResponse getPointBalance(Long memberId);

    PaginationResponse<PointHistoryResponse> getPointHistories(Long memberId, String type, int page, int size);
}
