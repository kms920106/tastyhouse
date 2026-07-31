package com.tastyhouse.domain.payment.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.payment.domain.vo.Amount;
import com.tastyhouse.domain.payment.domain.vo.PaymentId;
import com.tastyhouse.domain.payment.domain.vo.PaymentRefundId;

public record RefundRequestedEvent(
    PaymentRefundId refundId,
    PaymentId paymentId,
    MemberId memberId,
    Amount refundAmount,
    String refundReason,
    LocalDateTime requestedAt
) {
}
