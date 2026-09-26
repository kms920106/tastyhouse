package com.tastyhouse.application.payment.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record PgPaymentConfirmCommand(
    Long memberId,
    String pgProvider,
    String paymentKey,
    String pgOrderId,
    Integer amount
) {
    public PgPaymentConfirmCommand {
        if (memberId == null || pgProvider == null || paymentKey == null || pgOrderId == null || amount == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
