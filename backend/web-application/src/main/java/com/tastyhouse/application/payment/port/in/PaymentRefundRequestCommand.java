package com.tastyhouse.application.payment.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record PaymentRefundRequestCommand(
    Long memberId,
    Long paymentId,
    Integer refundAmount,
    String refundReason
) {

    public PaymentRefundRequestCommand {
        if (memberId == null || paymentId == null || refundAmount == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
