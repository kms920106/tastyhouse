package com.tastyhouse.application.payment.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record PaymentCancelCommand(
    Long memberId,
    Long paymentId,
    String cancelReason
) {

    public PaymentCancelCommand {
        if (memberId == null || paymentId == null || cancelReason == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
