package com.tastyhouse.core.domain.payment.application.dto.command;

public record RequestRefundCommand(
    int refundAmount,
    String refundReason
) {

    public static RequestRefundCommand of(int refundAmount, String refundReason) {
        return new RequestRefundCommand(refundAmount, refundReason);
    }
}
