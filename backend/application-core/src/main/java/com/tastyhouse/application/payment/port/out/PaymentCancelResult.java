package com.tastyhouse.application.payment.port.out;

public record PaymentCancelResult(
    String cancelCode,
    String message
) {
}
