package com.tastyhouse.application.payment.service;

import com.tastyhouse.domain.payment.vo.PaymentId;

public record PgReconcileResult(
    PaymentId paymentId,
    PgReconcileOutcome outcome
) {
}
