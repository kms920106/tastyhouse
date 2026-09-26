package com.tastyhouse.domain.payment.service;

public record PgConfirmationTarget(
    Long paymentId,
    String pgOrderId,
    int amount
) {
}
