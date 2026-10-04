package com.tastyhouse.application.payment.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record PaymentOnSiteCompleteCommand(
    Long memberId,
    Long paymentId
) {

    public PaymentOnSiteCompleteCommand {
        if (memberId == null || paymentId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static PaymentOnSiteCompleteCommand of(Long memberId, Long paymentId) {
        return new PaymentOnSiteCompleteCommand(memberId, paymentId);
    }
}
