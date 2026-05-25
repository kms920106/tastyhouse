package com.tastyhouse.core.domain.payment.application.dto.command;

import com.tastyhouse.core.domain.payment.domain.model.PaymentMethod;

public record CreatePaymentCommand(
    Long orderId,
    PaymentMethod paymentMethod
) {
}
