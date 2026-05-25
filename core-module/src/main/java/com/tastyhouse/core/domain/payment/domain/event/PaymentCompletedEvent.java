package com.tastyhouse.core.domain.payment.domain.event;

import com.tastyhouse.core.domain.payment.domain.model.PaymentMethod;
import com.tastyhouse.core.domain.payment.domain.vo.Amount;
import com.tastyhouse.core.domain.payment.domain.vo.OrderId;
import com.tastyhouse.core.domain.payment.domain.vo.PaymentId;

import java.time.LocalDateTime;

public record PaymentCompletedEvent(
    PaymentId paymentId,
    OrderId orderId,
    Long memberId,
    Amount amount,
    PaymentMethod paymentMethod,
    boolean isOnSitePayment,
    LocalDateTime completedAt
) {
}
