package com.tastyhouse.application.payment.port.out;

import java.time.LocalDateTime;

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

    public static PaymentRefundViewResult from(PaymentRefundResult result) {
        return new PaymentRefundViewResult(
            result.id(),
            result.paymentId(),
            result.refundAmount(),
            result.refundReason(),
            result.refundStatus(),
            result.pgRefundId(),
            result.refundedAt(),
            result.createdAt()
        );
    }
}
