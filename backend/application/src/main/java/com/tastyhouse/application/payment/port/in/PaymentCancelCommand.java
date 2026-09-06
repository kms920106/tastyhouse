package com.tastyhouse.application.payment.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record PaymentCancelCommand(
    Long memberId,
    Long paymentId,
    String cancelReason
) {
    public PaymentCancelCommand {
        if (memberId == null || paymentId == null || cancelReason == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
