package com.tastyhouse.application.payment.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

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
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
