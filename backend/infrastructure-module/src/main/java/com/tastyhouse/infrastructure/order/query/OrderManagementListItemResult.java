package com.tastyhouse.infrastructure.order.query;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.domain.shared.model.OrderMethod;

/**
 * 주문 관리 목록 항목 조회 결과(admin-api용) — 주문번호·가게명·주문자명·주문방식·주문상태·결제상태와
 * 최종 금액·상품 종류 수를 함께 투영한다.
 *
 * <p>회원 화면 목록({@link OrderListItemResult})과 같은 패키지에 공존하므로 {@code Management} 한정어를
 * 유지한다(공통 지침 패턴 3 — 모듈 분리로 충돌이 사라지지 않는다).
 */
public record OrderManagementListItemResult(
    Long id,
    String orderNumber,
    String shopName,
    String ordererName,
    OrderMethod orderMethod,
    OrderStatus orderStatus,
    PaymentStatus paymentStatus,
    Integer finalAmount,
    Integer totalItemCount,
    LocalDateTime createdAt,

    // 수령 예약 시각(슬롯 시작). null이면 즉시 주문.
    LocalDateTime scheduledAt
) {
    @QueryProjection
    public OrderManagementListItemResult {
    }
}
