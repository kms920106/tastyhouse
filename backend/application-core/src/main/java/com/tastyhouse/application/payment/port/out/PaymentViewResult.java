package com.tastyhouse.application.payment.port.out;

import java.time.LocalDateTime;

public record PaymentViewResult(
    Long id,
    Long orderId,
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

    public static PaymentViewResult from(PaymentResult result) {
        return new PaymentViewResult(
            result.id(),
            result.orderId(),
            result.paymentMethod(),
            result.paymentStatus(),
            result.amount(),
            result.pgProvider(),
            result.pgTid(),
            result.pgOrderId(),
            result.cardCompany(),
            result.cardNumber(),
            result.installmentMonths(),
            result.approvedAt(),
            result.cancelledAt(),
            result.cancelReason(),
            result.receiptUrl(),
            result.createdAt()
        );
    }
}
