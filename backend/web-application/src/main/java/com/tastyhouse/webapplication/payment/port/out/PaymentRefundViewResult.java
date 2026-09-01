package com.tastyhouse.webapplication.payment.port.out;

import java.time.LocalDateTime;

/**
 * 환불 요청 단건 화면 계약 — 환불 금액 VO와 상태 enum을 경계 타입으로 강등한 형태.
 *
 * <p><b>챕터 10</b>에서 신설. 거처와 근거는 {@link PaymentViewResult}와 같다 —
 * {@code refundAmount()}가 Money VO라 {@code .value()} 언랩이 필요하고, null이면 null을 유지한다.
 */
public record PaymentRefundViewResult(
    Long id,
    Long paymentId,
    Integer refundAmount,
    String refundReason,
    String refundStatus,
    String pgRefundId,
    LocalDateTime refundedAt,
    LocalDateTime createdAt
) {
}
