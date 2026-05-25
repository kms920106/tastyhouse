package com.tastyhouse.webapi.payment.response;

import com.tastyhouse.core.domain.payment.application.dto.result.PaymentRefundResult;
import com.tastyhouse.core.domain.payment.domain.model.RefundStatus;

import java.time.LocalDateTime;

public record PaymentRefundResponse(
    Long id,
    Long paymentId,
    Integer refundAmount,
    String refundReason,
    RefundStatus refundStatus,
    String pgRefundId,
    LocalDateTime refundedAt,
    LocalDateTime createdAt
) {
    public static PaymentRefundResponse from(PaymentRefundResult result) {
        return new PaymentRefundResponse(
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
