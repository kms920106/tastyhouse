package com.tastyhouse.adminapplication.shop.port.in;

import java.util.Optional;

import com.tastyhouse.application.shop.port.out.ShopOrderNoticeResult;

/**
 * 가게 주문 안내 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ShopOrderNoticeQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 *
 * <p><b>챕터 06</b> — 반환 타입은 Swagger를 아는 {@code *Response}가 아니라 프레임워크-프리
 * {@code *Result}다. 미등록 가게를 뜻하는 빈 응답 조립도 컨트롤러가 담당하므로 {@code Optional}을 그대로 넘긴다.
 */
public interface ShopOrderNoticeQueryUseCase {

    Optional<ShopOrderNoticeResult> getOrderNotice(Long shopId);
}
