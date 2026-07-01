package com.tastyhouse.core.domain.payment.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.payment.domain.vo.Amount;
import com.tastyhouse.core.domain.payment.domain.vo.PaymentId;
import com.tastyhouse.core.domain.payment.domain.vo.PaymentRefundId;

public record RefundRequestedEvent(
    PaymentRefundId refundId,
    PaymentId paymentId,
    Long memberId,
    Amount refundAmount,
    String refundReason,
    LocalDateTime requestedAt
) {
}
