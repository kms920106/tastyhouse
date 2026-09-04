package com.tastyhouse.adminapplication.point.port.in;

import java.util.Optional;

import com.tastyhouse.application.point.port.out.PointBalanceResult;
import com.tastyhouse.application.point.port.out.PointHistoryResult;
import com.tastyhouse.domain.shared.page.PageResult;

/**
 * 포인트 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code PointQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 *
 * <p><b>챕터 06</b> — 반환 타입은 Swagger를 아는 {@code *Response}가 아니라 프레임워크-프리
 * {@code *Result}다. Response 조립과 {@code PaginationResponse} 매핑은 컨트롤러가 담당한다.
 * 잔액은 이력이 없는 회원에 대해 빈 {@code Optional}이며, 0 응답으로 대체하는 것도 컨트롤러의 몫이다
 * (기존 {@code PointBalanceResponse.zero(memberId)} 동작 보존).
 */
public interface PointQueryUseCase {

    Optional<PointBalanceResult> getPointBalance(Long memberId);

    PageResult<PointHistoryResult> getPointHistories(Long memberId, String type, int page, int size);
}
