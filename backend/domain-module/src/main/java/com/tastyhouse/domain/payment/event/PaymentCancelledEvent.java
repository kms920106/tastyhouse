package com.tastyhouse.domain.payment.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.vo.PaymentId;

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
