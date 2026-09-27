package com.tastyhouse.application.payment.port.out;

import java.time.LocalDateTime;

public record PaymentResult(
    Long id,
    Long orderId,
    Long memberId,
    String paymentMethod,
    String paymentStatus,
    Integer amount,
    String pgProvider,
    String pgTid,
    String pgOrderId,
    String cardCompany,
    String cardNumber,
    Integer installmentMonths,
    LocalDateTime approvedAt,
    LocalDateTime cancelledAt,
    String cancelReason,
    String receiptUrl,
    LocalDateTime createdAt
) {
}
