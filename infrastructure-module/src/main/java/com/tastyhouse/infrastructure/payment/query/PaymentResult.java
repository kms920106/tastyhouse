package com.tastyhouse.infrastructure.payment.query;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.payment.domain.model.PaymentMethod;
import com.tastyhouse.core.domain.payment.domain.model.PaymentStatus;
import com.tastyhouse.core.domain.payment.domain.model.PgProvider;
import com.tastyhouse.core.domain.payment.domain.vo.Amount;

/**
 * 결제 단건 조회 결과.
 *
 * <p>{@code memberId}는 회원 스코프 검증용으로 주문에서 함께 투영한다 — 소비 모듈
 * ({@code webapi.payment.PaymentQueryService})이 요청 회원과 대조해 남의 결제 열람을 막는다
 * ({@code OrderDetailResult}와 동일한 방식).
 *
 * <p>{@code amount}는 {@code PAYMENT.amount} 컬럼이 {@code @Convert}로 {@link Amount} VO에 매핑되어
 * 있어 QueryDSL이 VO 타입 path를 생성하므로 그 타입을 그대로 받는다(소비 모듈이 {@code value()}로 꺼낸다).
 */
public record PaymentResult(
    Long id,
    Long orderId,
    MemberId memberId,
    PaymentMethod paymentMethod,
    PaymentStatus paymentStatus,
    Amount amount,
    PgProvider pgProvider,
    String pgTid,
    String pgOrderId,
    String cardCompany,
    String cardNumber,
    Integer installmentMonths,
    LocalDateTime approvedAt,
    LocalDateTime cancelledAt,
    String cancelReason,
    String receiptUrl,
    LocalDateTime createdAt
) {
    @QueryProjection
    public PaymentResult {
    }
}
