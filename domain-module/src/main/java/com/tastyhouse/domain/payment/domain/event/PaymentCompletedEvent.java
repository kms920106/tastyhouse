package com.tastyhouse.domain.payment.domain.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.order.domain.vo.OrderId;
import com.tastyhouse.domain.payment.domain.model.PaymentMethod;
import com.tastyhouse.domain.payment.domain.vo.Amount;
import com.tastyhouse.domain.payment.domain.vo.PaymentId;

public record PaymentCompletedEvent(
    PaymentId paymentId,
    OrderId orderId,
    MemberId memberId,
    Amount amount,
    PaymentMethod paymentMethod,
    boolean isOnSitePayment,
    LocalDateTime completedAt
) {
}
