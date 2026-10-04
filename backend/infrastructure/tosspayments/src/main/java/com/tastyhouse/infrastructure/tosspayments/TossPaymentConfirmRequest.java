package com.tastyhouse.infrastructure.tosspayments;

record TossPaymentConfirmRequest(
    String paymentKey,
    Integer amount,
    String orderId
) {
}
