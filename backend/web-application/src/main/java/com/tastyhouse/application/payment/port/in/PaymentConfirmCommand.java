package com.tastyhouse.application.payment.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record PaymentConfirmCommand(
    Long paymentId,
    String pgProvider,
    String pgTid,
    String pgOrderId,
    String cardCompany,
    String cardNumber,
    Integer installmentMonths,
    String receiptUrl
) {

    public PaymentConfirmCommand {
        if (paymentId == null || pgProvider == null || pgTid == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
