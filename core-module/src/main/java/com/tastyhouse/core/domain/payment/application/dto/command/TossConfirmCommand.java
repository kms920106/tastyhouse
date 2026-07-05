package com.tastyhouse.core.domain.payment.application.dto.command;

public record TossConfirmCommand(
    String paymentKey,
    String pgOrderId,
    int amount
) {

    public static TossConfirmCommand of(String paymentKey, String pgOrderId, int amount) {
        return new TossConfirmCommand(paymentKey, pgOrderId, amount);
    }
}
