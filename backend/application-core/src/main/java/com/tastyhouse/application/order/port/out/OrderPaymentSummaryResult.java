package com.tastyhouse.application.order.port.out;

import java.time.LocalDateTime;

public record OrderPaymentSummaryResult(
    Long id,
    String paymentMethod,
    String paymentStatus,
    Integer amount,
    Integer cupDepositAmount,
    String cardCompany,
    String cardNumber,
    LocalDateTime approvedAt,
    String receiptUrl
) {
}
