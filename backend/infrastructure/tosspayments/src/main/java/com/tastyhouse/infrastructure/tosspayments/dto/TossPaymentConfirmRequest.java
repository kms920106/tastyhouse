package com.tastyhouse.infrastructure.tosspayments.dto;

public record TossPaymentConfirmRequest(
    String paymentKey,
    Integer amount,
    String orderId
) {
}
