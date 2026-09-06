package com.tastyhouse.application.payment.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record PaymentOnSiteCompleteCommand(
    Long memberId,
    Long paymentId
) {
    public PaymentOnSiteCompleteCommand {
        if (memberId == null || paymentId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static PaymentOnSiteCompleteCommand of(Long memberId, Long paymentId) {
        return new PaymentOnSiteCompleteCommand(memberId, paymentId);
    }
}
