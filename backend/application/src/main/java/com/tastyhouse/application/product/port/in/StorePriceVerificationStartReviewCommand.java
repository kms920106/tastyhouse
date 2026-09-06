package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record StorePriceVerificationStartReviewCommand(Long verificationId) {
    public StorePriceVerificationStartReviewCommand {
        if (verificationId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static StorePriceVerificationStartReviewCommand of(Long verificationId) {
        return new StorePriceVerificationStartReviewCommand(verificationId);
    }
}
