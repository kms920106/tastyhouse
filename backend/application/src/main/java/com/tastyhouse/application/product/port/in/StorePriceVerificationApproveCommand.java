package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record StorePriceVerificationApproveCommand(Long verificationId) {
    public StorePriceVerificationApproveCommand {
        if (verificationId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static StorePriceVerificationApproveCommand of(Long verificationId) {
        return new StorePriceVerificationApproveCommand(verificationId);
    }
}
