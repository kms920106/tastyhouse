package com.tastyhouse.external.payment.toss.dto;

import java.time.LocalDateTime;

public record TossPaymentCancelResult(
    boolean success,
    String paymentKey,
    String orderId,
    String orderName,
    String status,
    Integer totalAmount,
    Integer balanceAmount,
    String cancelReason,
    LocalDateTime canceledAt,
    Integer cancelAmount,
    Integer refundableAmount,
    String cancelStatus,
    String errorCode,
    String errorMessage
) {
}
