package com.tastyhouse.application.payment.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.payment.model.PaymentMethod;
import com.tastyhouse.domain.payment.model.PaymentStatus;
import com.tastyhouse.domain.payment.model.PgProvider;
import com.tastyhouse.domain.payment.vo.Amount;

public record PaymentResult(
    Long id,
    Long orderId,
    Long memberId,
    PaymentMethod paymentMethod,
    PaymentStatus paymentStatus,
    Amount amount,
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
    LocalDateTime createdAt
) {
}
