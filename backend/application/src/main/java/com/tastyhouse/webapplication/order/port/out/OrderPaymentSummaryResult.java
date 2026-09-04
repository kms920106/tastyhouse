package com.tastyhouse.webapplication.order.port.out;

import java.time.LocalDateTime;

/**
 * 주문 상세의 결제 요약 — 결제 투영에 주문의 보증금액을 합쳐 담는다.
 *
 * <p><b>챕터 10</b>에서 신설. <b>보증금은 결제(PAYMENT) 테이블이 아니라 주문에 저장된다</b> —
 * {@code PAYMENT.amount}는 손님이 실제로 내는 돈(보증금 포함 {@code final_amount})이고, 그중 얼마가
 * 보증금인지는 주문이 안다. 그래서 이 계약은 {@code OrderPaymentResult}(결제 포트 투영)와
 * {@code OrderDetailResult.cupDepositAmount}(주문 투영) <b>두 곳</b>에서 값을 받는다. 두 출처를
 * 합치는 것이므로 공유 읽기 계약 패키지에 형제로 둘 수 없다.
 *
 * <p>enum 강등(결제수단·결제상태)은 서비스에서 끝낸다 — null이면 null을 그대로 유지하는 것이
 * 기존 동작이다.
 */
public record OrderPaymentSummaryResult(
    Long id,
    String paymentMethod,
    String paymentStatus,
    Integer amount,
    Integer cupDepositAmount,
    String cardCompany,
    String cardNumber,
    LocalDateTime approvedAt,
    String receiptUrl
) {
}
