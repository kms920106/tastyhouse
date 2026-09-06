package com.tastyhouse.application.payment.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record PaymentCreateCommand(
    Long memberId,
    Long orderId,
    String paymentMethod
) {
    public PaymentCreateCommand {
        if (memberId == null || orderId == null || paymentMethod == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
