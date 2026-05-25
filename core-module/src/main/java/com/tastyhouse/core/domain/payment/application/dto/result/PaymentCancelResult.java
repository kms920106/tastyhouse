package com.tastyhouse.core.domain.payment.application.dto.result;

import com.tastyhouse.core.domain.payment.domain.model.PaymentCancelCode;

public record PaymentCancelResult(
    PaymentCancelCode code,
    String message
) {
    public static PaymentCancelResult of(PaymentCancelCode code) {
        return new PaymentCancelResult(code, code.getMessage());
    }
}
