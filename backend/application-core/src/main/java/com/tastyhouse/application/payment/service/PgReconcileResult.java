package com.tastyhouse.application.payment.service;

import com.tastyhouse.domain.payment.vo.PaymentId;

record PgReconcileResult(
    PaymentId paymentId,
    PgReconcileOutcome outcome
) {
}
