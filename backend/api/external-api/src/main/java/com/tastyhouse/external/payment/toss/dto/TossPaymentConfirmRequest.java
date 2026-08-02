package com.tastyhouse.external.payment.toss.dto;

public record TossPaymentConfirmRequest(
    String paymentKey,
    Integer amount,
    String orderId
) {
}
