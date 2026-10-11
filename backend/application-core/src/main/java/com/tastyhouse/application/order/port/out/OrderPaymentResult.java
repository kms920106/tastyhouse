package com.tastyhouse.application.order.port.out;

import java.time.LocalDateTime;

public record OrderPaymentResult(
    Long id,
    String paymentMethod,
    String paymentStatus,
    Integer amount,
    String cardCompany,
    String cardNumber,
    LocalDateTime approvedAt,
    String receiptUrl
) {
}
