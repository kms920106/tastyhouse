package com.tastyhouse.core.domain.payment.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.order.domain.vo.OrderId;
import com.tastyhouse.core.domain.payment.domain.vo.PaymentId;

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
