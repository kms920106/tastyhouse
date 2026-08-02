package com.tastyhouse.domain.payment.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.payment.model.PaymentMethod;
import com.tastyhouse.domain.payment.vo.Amount;
import com.tastyhouse.domain.payment.vo.PaymentId;

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
