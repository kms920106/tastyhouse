package com.tastyhouse.application.payment.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record PaymentOnSiteCompleteCommand(
    Long ceoId,
    Long shopId,
    Long paymentId
) {

    public PaymentOnSiteCompleteCommand {
        if (ceoId == null || shopId == null || paymentId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static PaymentOnSiteCompleteCommand of(Long ceoId, Long shopId, Long paymentId) {
        return new PaymentOnSiteCompleteCommand(ceoId, shopId, paymentId);
    }
}
