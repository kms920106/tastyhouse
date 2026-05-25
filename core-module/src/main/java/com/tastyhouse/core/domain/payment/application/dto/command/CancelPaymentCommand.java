package com.tastyhouse.core.domain.payment.application.dto.command;

public record CancelPaymentCommand(
    String cancelReason
) {
}
