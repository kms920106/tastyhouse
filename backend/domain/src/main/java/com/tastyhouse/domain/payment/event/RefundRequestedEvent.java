package com.tastyhouse.domain.payment.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.payment.vo.Amount;
import com.tastyhouse.domain.payment.vo.PaymentId;
import com.tastyhouse.domain.payment.vo.PaymentRefundId;

public record RefundRequestedEvent(
    PaymentRefundId refundId,
    PaymentId paymentId,
    MemberId memberId,
    Amount refundAmount,
    String refundReason,
    LocalDateTime requestedAt
) {
}
