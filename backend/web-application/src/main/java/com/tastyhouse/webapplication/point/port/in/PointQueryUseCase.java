package com.tastyhouse.webapplication.point.port.in;

import com.tastyhouse.webapplication.point.response.PointHistoryResponse;
import com.tastyhouse.webapplication.point.response.PointResponse;
import com.tastyhouse.webapplication.point.response.PointUsableResponse;

/**
 * 포인트 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code PointQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface PointQueryUseCase {

    PointResponse getMemberPoint(Long memberId);

    PointHistoryResponse getPointHistory(Long memberId);

    PointUsableResponse getUsablePoint(Long memberId);
}
