package com.tastyhouse.application.payment.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record PaymentRefundRequestCommand(
    Long memberId,
    Long paymentId,
    Integer refundAmount,
    String refundReason
) {
    public PaymentRefundRequestCommand {
        if (memberId == null || paymentId == null || refundAmount == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
