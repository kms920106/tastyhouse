package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record StorePriceVerificationRejectCommand(Long verificationId, String rejectReason) {

    public StorePriceVerificationRejectCommand {
        if (verificationId == null || rejectReason == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
