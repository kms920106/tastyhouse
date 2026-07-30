package com.tastyhouse.infrastructure.order.query;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.core.domain.payment.domain.model.PaymentMethod;
import com.tastyhouse.core.domain.payment.domain.model.PaymentStatus;
import com.tastyhouse.core.domain.payment.domain.vo.Amount;

/**
 * 주문 상세에 함께 노출하는 결제 요약 조회 결과.
 *
 * <p>{@code amount}는 {@code PAYMENT.amount} 컬럼이 {@code @Convert}로 {@link Amount} VO에 매핑되어
 * 있어 QueryDSL이 VO 타입 path를 생성하므로, 그 타입을 그대로 받는다(소비 모듈이 {@code value()}로 꺼낸다).
 */
public record OrderPaymentResult(
    Long id,
    PaymentMethod paymentMethod,
    PaymentStatus paymentStatus,
    Amount amount,
    String cardCompany,
    String cardNumber,
    LocalDateTime approvedAt,
    String receiptUrl
) {
    @QueryProjection
    public OrderPaymentResult {
    }
}
