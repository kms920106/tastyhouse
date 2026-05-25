package com.tastyhouse.core.domain.payment.application.dto.result;

import com.tastyhouse.core.domain.payment.domain.model.PaymentRefund;
import com.tastyhouse.core.domain.payment.domain.model.RefundStatus;

import java.time.LocalDateTime;

public record PaymentRefundResult(
    Long id,
    Long paymentId,
    Integer refundAmount,
    String refundReason,
    RefundStatus refundStatus,
    String pgRefundId,
    LocalDateTime refundedAt,
    LocalDateTime createdAt
) {
    public static PaymentRefundResult from(PaymentRefund refund) {
        return new PaymentRefundResult(
            refund.getId(),
            refund.getPaymentId() != null ? refund.getPaymentId().value() : null,
            refund.getRefundAmount() != null ? refund.getRefundAmount().value() : null,
            refund.getRefundReason(),
            refund.getRefundStatus(),
            refund.getPgRefundId(),
            refund.getRefundedAt(),
            refund.getCreatedAt()
        );
    }
}
