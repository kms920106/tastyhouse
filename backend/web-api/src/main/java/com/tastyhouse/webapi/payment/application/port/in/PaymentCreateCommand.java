package com.tastyhouse.webapi.payment.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 결제 개시 command. {@code paymentMethod}의 enum 승격은 서비스가 담당한다.
 */
public record PaymentCreateCommand(
    Long memberId,
    Long orderId,
    String paymentMethod
) {
    public PaymentCreateCommand {
        if (memberId == null || orderId == null || paymentMethod == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
