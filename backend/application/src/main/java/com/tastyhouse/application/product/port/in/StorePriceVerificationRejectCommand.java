package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record StorePriceVerificationRejectCommand(Long verificationId, String rejectReason) {
    public StorePriceVerificationRejectCommand {
        if (verificationId == null || rejectReason == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
