package com.tastyhouse.application.payment.port.out.write;

import java.time.LocalDateTime;

public record PaymentRefundState(
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
