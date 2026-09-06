package com.tastyhouse.infrastructure.order.query;

import java.time.LocalDateTime;

import com.tastyhouse.domain.payment.model.PaymentMethod;
import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.domain.payment.vo.Amount;

public record PaymentProjection(
    Long id,
    PaymentMethod paymentMethod,
    PaymentStatus paymentStatus,
    Amount amount,
    String cardCompany,
    String cardNumber,
    LocalDateTime approvedAt,
    String receiptUrl
) {
}
