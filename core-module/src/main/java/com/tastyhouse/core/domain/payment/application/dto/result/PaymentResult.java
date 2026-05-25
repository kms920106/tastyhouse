package com.tastyhouse.core.domain.payment.application.dto.result;

import com.tastyhouse.core.domain.payment.domain.model.Payment;
import com.tastyhouse.core.domain.payment.domain.model.PaymentMethod;
import com.tastyhouse.core.domain.payment.domain.model.PaymentStatus;
import com.tastyhouse.core.domain.payment.domain.model.PgProvider;

import java.time.LocalDateTime;

public record PaymentResult(
    Long id,
    Long orderId,
    PaymentMethod paymentMethod,
    PaymentStatus paymentStatus,
    Integer amount,
    PgProvider pgProvider,
    String pgTid,
    String pgOrderId,
    String cardCompany,
    String cardNumber,
    Integer installmentMonths,
    LocalDateTime approvedAt,
    LocalDateTime cancelledAt,
    String cancelReason,
    String receiptUrl,
    String cashReceiptNumber,
    String cashReceiptType,
    LocalDateTime createdAt
) {
    public static PaymentResult from(Payment payment) {
        return new PaymentResult(
            payment.getId(),
            payment.getOrderId() != null ? payment.getOrderId().value() : null,
            payment.getPaymentMethod(),
            payment.getPaymentStatus(),
            payment.getAmount() != null ? payment.getAmount().value() : null,
            payment.getPgProvider(),
            payment.getPgTid(),
            payment.getPgOrderId(),
            payment.getCardCompany(),
            payment.getCardNumber(),
            payment.getInstallmentMonths(),
            payment.getApprovedAt(),
            payment.getCancelledAt(),
            payment.getCancelReason(),
            payment.getReceiptUrl(),
            payment.getCashReceiptNumber(),
            payment.getCashReceiptType(),
            payment.getCreatedAt()
        );
    }
}
