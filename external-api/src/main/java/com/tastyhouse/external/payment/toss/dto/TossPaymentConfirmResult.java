package com.tastyhouse.external.payment.toss.dto;

import java.time.LocalDateTime;

public record TossPaymentConfirmResult(
    boolean success,
    String paymentKey,
    String orderId,
    String orderName,
    Integer totalAmount,
    String status,
    LocalDateTime approvedAt,
    String receiptUrl,
    String cardCompany,
    String cardNumber,
    Integer installmentPlanMonths,
    Boolean isInterestFree,
    String cardType,
    String method,
    String errorCode,
    String errorMessage
) {
}
