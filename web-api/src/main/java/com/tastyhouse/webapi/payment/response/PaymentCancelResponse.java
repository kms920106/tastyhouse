package com.tastyhouse.webapi.payment.response;

import com.tastyhouse.core.domain.payment.application.dto.result.PaymentCancelResult;

public record PaymentCancelResponse(
    String code,
    String message
) {
    public static PaymentCancelResponse of(PaymentCancelResult result) {
        return new PaymentCancelResponse(result.code().name(), result.message());
    }
}
