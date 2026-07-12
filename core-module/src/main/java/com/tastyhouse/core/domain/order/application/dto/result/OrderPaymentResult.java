package com.tastyhouse.core.domain.order.application.dto.result;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.payment.domain.model.Payment;
import com.tastyhouse.core.domain.payment.domain.model.PaymentMethod;
import com.tastyhouse.core.domain.payment.domain.model.PaymentStatus;

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
    public static OrderPaymentResult from(Payment payment) {
        return new OrderPaymentResult(
            payment.getId(),
            payment.getPaymentMethod(),
            payment.getPaymentStatus(),
            payment.getAmount() != null ? payment.getAmount().value() : null,
            payment.getCardCompany(),
            payment.getCardNumber(),
            payment.getApprovedAt(),
            payment.getReceiptUrl()
        );
    }
}
