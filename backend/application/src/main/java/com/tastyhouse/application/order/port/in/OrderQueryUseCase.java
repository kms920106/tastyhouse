package com.tastyhouse.application.order.port.in;

import com.tastyhouse.application.order.port.out.OrderListItemResult;
import com.tastyhouse.application.shared.marker.WebApp;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.order.port.out.OrderDetailViewResult;

/**
 * 주문 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code OrderQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 *
 * <p><b>챕터 10</b> — 반환 타입이 표현 계약에서 프레임워크-프리 읽기 계약으로 바뀌었다. 본인 주문
 * 검증({@code ORDER_ACCESS_DENIED})과 리뷰 여부 배치 조회는 여전히 구현이 하고, 표현 계약 조립만
 * web-api로 올라갔다. 금액 필드는 어느 쪽에서도 계산하지 않는다 — DAO 투영값을 그대로 나른다.
 */
@WebApp
public interface OrderQueryUseCase {

    PageResult<OrderListItemResult> getOrderList(Long memberId, int page, int size);

    OrderDetailViewResult getOrderDetail(Long memberId, Long orderId);
}
