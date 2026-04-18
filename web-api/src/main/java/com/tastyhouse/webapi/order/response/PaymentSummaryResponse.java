package com.tastyhouse.webapi.order.response;

import com.tastyhouse.core.entity.payment.PaymentMethod;
import com.tastyhouse.core.entity.payment.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentSummaryResponse(
    Long id,
    PaymentMethod paymentMethod,
    PaymentStatus paymentStatus,
    Integer amount,
    String cardCompany,
    String cardNumber,
    LocalDateTime approvedAt,
    String receiptUrl
) {
    public static PaymentSummaryResponse from(
    Long id,
    PaymentMethod paymentMethod,
    PaymentStatus paymentStatus,
    Integer amount,
    String cardCompany,
    String cardNumber,
    LocalDateTime approvedAt,
    String receiptUrl
    ) {
    return new PaymentSummaryResponse(
        id,
        paymentMethod,
        paymentStatus,
        amount,
        cardCompany,
        cardNumber,
        approvedAt,
        receiptUrl
    );
    }
}
