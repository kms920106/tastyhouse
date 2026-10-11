package com.tastyhouse.application.payment.service;

public record PgConfirmationTarget(
    Long paymentId,
    String pgOrderId,
    int amount
) {
}
