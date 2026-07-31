package com.tastyhouse.domain.payment.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.order.domain.vo.OrderId;
import com.tastyhouse.domain.payment.domain.vo.PaymentId;

public record PaymentCancelledEvent(
    PaymentId paymentId,
    OrderId orderId,
    MemberId memberId,
    int usedPoint,
    int earnedPoint,
    String cancelReason,
    LocalDateTime cancelledAt
) {
}
