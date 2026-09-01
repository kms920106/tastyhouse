package com.tastyhouse.infrastructure.order.query;

import java.time.LocalDateTime;

import com.tastyhouse.domain.payment.model.PaymentMethod;
import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.domain.payment.vo.Amount;

/**
 * 결제 요약의 <b>infra 내부 중간 투영</b> — {@code Amount} VO를 그대로 받는다.
 *
 * <p>읽기 계약 {@code OrderPaymentResult}는 경계 타입 {@code Integer}를 싣지만
 * ({@code PAYMENT.amount}가 {@code @Convert} 매핑이라) QueryDSL이 생성하는 path는
 * {@code SimplePath<Amount>}이므로, 투영 단계에서는 VO로 받고
 * {@code OrderQueryDao#withUnwrappedAmount}가 fetch 직후 언랩한다.
 *
 * <p><b>반드시 {@code public}이어야 한다</b> — {@code Projections.constructor}는
 * {@code Class#getConstructors()} 리플렉션으로 생성자를 찾고 그것은 public 생성자만 반환하므로,
 * package-private이면 같은 패키지의 DAO가 투영해도 <b>컴파일은 통과하고 그 쿼리가 실행되는
 * 순간에만</b> {@code ExpressionException}으로 실패한다
 * ({@code ShopRiderGuidePickupPresenceResult} 장애 선례).
 */
public record PaymentProjection(
    Long id,
    PaymentMethod paymentMethod,
    PaymentStatus paymentStatus,
    Amount amount,
    String cardCompany,
    String cardNumber,
    LocalDateTime approvedAt,
    String receiptUrl
) {
}
