package com.tastyhouse.domain.payment.service;

public record TossConfirmationTarget(
    Long paymentId,
    String pgOrderId,
    int amount
) {
}
