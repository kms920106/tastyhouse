package com.tastyhouse.core.domain.payment.application.dto.command;

public record TossConfirmCommand(
    String paymentKey,
    String pgOrderId,
    int amount
) {
}
