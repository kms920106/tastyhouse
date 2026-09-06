package com.tastyhouse.application.order.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.payment.model.PaymentMethod;
import com.tastyhouse.domain.payment.model.PaymentStatus;

public record OrderPaymentResult(
    Long id,
    PaymentMethod paymentMethod,
    PaymentStatus paymentStatus,
    Integer amount,
    String cardCompany,
    String cardNumber,
    LocalDateTime approvedAt,
    String receiptUrl
) {
}
