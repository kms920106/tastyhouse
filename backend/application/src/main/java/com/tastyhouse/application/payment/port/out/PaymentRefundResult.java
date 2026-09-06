package com.tastyhouse.application.payment.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.payment.model.RefundStatus;
import com.tastyhouse.domain.payment.vo.Amount;

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
}
