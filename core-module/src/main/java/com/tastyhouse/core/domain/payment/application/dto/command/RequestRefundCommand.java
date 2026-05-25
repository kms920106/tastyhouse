package com.tastyhouse.core.domain.payment.application.dto.command;

public record RequestRefundCommand(
    int refundAmount,
    String refundReason
) {
}
