package com.tastyhouse.core.domain.payment.domain.event;

import com.tastyhouse.core.domain.order.domain.vo.OrderId;
import com.tastyhouse.core.domain.payment.domain.vo.PaymentId;

import java.time.LocalDateTime;

public record PaymentCancelledEvent(
    PaymentId paymentId,
    OrderId orderId,
    Long memberId,
    int usedPoint,
    int earnedPoint,
    String cancelReason,
    LocalDateTime cancelledAt
) {
}
