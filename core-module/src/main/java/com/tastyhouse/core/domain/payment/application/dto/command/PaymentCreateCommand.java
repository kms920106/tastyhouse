package com.tastyhouse.core.domain.payment.application.dto.command;

import com.tastyhouse.core.domain.payment.domain.model.PaymentMethod;

public record PaymentCreateCommand(
    Long orderId,
    PaymentMethod paymentMethod
) {

    public static PaymentCreateCommand of(Long orderId, PaymentMethod paymentMethod) {
        return new PaymentCreateCommand(orderId, paymentMethod);
    }
}
