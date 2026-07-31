package com.tastyhouse.infrastructure.payment.query;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.core.domain.payment.domain.model.RefundStatus;
import com.tastyhouse.core.domain.payment.domain.vo.Amount;

/**
 * 결제 환불 단건 조회 결과.
 *
 * <p>{@code paymentId}·{@code refundAmount}는 {@code PAYMENT_REFUND}의 해당 컬럼이 {@code @Convert}로
 * VO에 매핑되어 있어 QueryDSL이 VO 타입 path를 생성하므로 raw {@code Long}으로 우회해 받는다
 * ({@code refundAmount}는 {@link Amount} VO 그대로 받는다 — 소비 모듈이 {@code value()}로 꺼낸다).
 */
public record PaymentRefundResult(
    Long id,
    Long paymentId,
    Amount refundAmount,
    String refundReason,
    RefundStatus refundStatus,
    String pgRefundId,
    LocalDateTime refundedAt,
    LocalDateTime createdAt
) {
    @QueryProjection
    public PaymentRefundResult {
    }
}
