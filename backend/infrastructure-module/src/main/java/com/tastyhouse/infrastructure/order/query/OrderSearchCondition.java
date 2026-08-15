package com.tastyhouse.infrastructure.order.query;

import java.time.LocalDateTime;

import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.domain.shared.model.OrderMethod;

/**
 * 주문 관리 목록 검색 조건(admin-api용) — 모든 필드가 null 허용이며, null인 항목은 조건에서 제외된다.
 *
 * <p>enum 필드는 도메인 enum이므로 HTTP 경계의 {@code String}을 소비 모듈의 {@code OrderQueryService}가
 * {@code Enum.from(...)}으로 승격해 담는다(도메인 enum 경계 규칙).
 */
public record OrderSearchCondition(
    Long shopId,
    OrderStatus orderStatus,
    OrderMethod orderMethod,
    PaymentStatus paymentStatus,
    String orderNumber,
    String ordererName,
    LocalDateTime startDate,
    LocalDateTime endDate
) {

    public static OrderSearchCondition of(
        Long shopId,
        OrderStatus orderStatus,
        OrderMethod orderMethod,
        PaymentStatus paymentStatus,
        String orderNumber,
        String ordererName,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        return new OrderSearchCondition(
            shopId,
            orderStatus,
            orderMethod,
            paymentStatus,
            orderNumber,
            ordererName,
            startDate,
            endDate
        );
    }
}
