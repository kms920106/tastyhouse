package com.tastyhouse.external.tosspayments.dto;

public record TossPaymentConfirmRequest(
    String paymentKey,
    Integer amount,
    String orderId
) {
}
