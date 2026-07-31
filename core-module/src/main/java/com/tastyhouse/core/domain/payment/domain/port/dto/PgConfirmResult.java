package com.tastyhouse.core.domain.payment.domain.port.dto;

import java.time.LocalDateTime;

public record PgConfirmResult(
    boolean success,
    String paymentKey,
    String status,
    Integer totalAmount,
    LocalDateTime approvedAt,
    String receiptUrl,
    String cardCompany,
    String cardNumber,
    Integer installmentPlanMonths,
    String errorCode,
    String errorMessage,
    TossPaymentDetail detail
) {
}
