package com.tastyhouse.application.payment.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record PaymentCreateCommand(
    Long memberId,
    Long orderId,
    String paymentMethod
) {

    public PaymentCreateCommand {
        if (memberId == null || orderId == null || paymentMethod == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
