package com.tastyhouse.application.payment.port.out;

import java.time.LocalDateTime;

public record PaymentRefundResult(
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
