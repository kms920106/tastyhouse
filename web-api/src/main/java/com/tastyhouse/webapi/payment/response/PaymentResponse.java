package com.tastyhouse.webapi.payment.response;

import com.tastyhouse.core.domain.payment.application.dto.result.PaymentResult;
import com.tastyhouse.core.domain.payment.domain.model.PaymentMethod;
import com.tastyhouse.core.domain.payment.domain.model.PaymentStatus;
import com.tastyhouse.core.domain.payment.domain.model.PgProvider;

import java.time.LocalDateTime;

public record PaymentResponse(
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
    public static PaymentResponse from(PaymentResult result) {
        return new PaymentResponse(
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
            result.cashReceiptNumber(),
            result.cashReceiptType(),
            result.createdAt()
        );
    }
}
