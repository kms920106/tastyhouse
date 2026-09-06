package com.tastyhouse.application.payment.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record TossPaymentConfirmCommand(
    Long memberId,
    String paymentKey,
    String pgOrderId,
    Integer amount
) {
    public TossPaymentConfirmCommand {
        if (memberId == null || paymentKey == null || pgOrderId == null || amount == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
