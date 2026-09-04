package com.tastyhouse.application.payment.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 결제 취소 command. 경로 변수 {@code paymentId}와 본문의 취소 사유를 함께 담는다.
 */
public record PaymentCancelCommand(
    Long memberId,
    Long paymentId,
    String cancelReason
) {
    public PaymentCancelCommand {
        if (memberId == null || paymentId == null || cancelReason == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
