package com.tastyhouse.application.payment.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record PgPaymentConfirmCommand(
    Long memberId,
    String pgProvider,
    String paymentKey,
    String pgOrderId,
    Integer amount
) {

    public PgPaymentConfirmCommand {
        if (memberId == null || pgProvider == null || paymentKey == null || pgOrderId == null || amount == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
