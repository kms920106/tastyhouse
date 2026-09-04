package com.tastyhouse.application.point.port.in;

import com.tastyhouse.application.point.port.out.PointBalanceResult;
import com.tastyhouse.application.shared.marker.WebApp;
import com.tastyhouse.application.point.port.out.PointHistoryViewResult;

/**
 * 포인트 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code PointQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
@WebApp
public interface PointQueryUseCase {

    PointBalanceResult getMemberPoint(Long memberId);

    PointHistoryViewResult getPointHistory(Long memberId);

    /**
     * @return 주문에 사용할 수 있는 포인트(잔액 없는 회원은 0)
     */
    Integer getUsablePoint(Long memberId);
}
