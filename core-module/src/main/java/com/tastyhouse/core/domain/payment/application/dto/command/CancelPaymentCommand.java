package com.tastyhouse.core.domain.payment.application.dto.command;

public record CancelPaymentCommand(
    String cancelReason
) {

    public static CancelPaymentCommand of(String cancelReason) {
        return new CancelPaymentCommand(cancelReason);
    }
}
