package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record StorePriceVerificationApproveCommand(Long verificationId) {

    public StorePriceVerificationApproveCommand {
        if (verificationId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static StorePriceVerificationApproveCommand of(Long verificationId) {
        return new StorePriceVerificationApproveCommand(verificationId);
    }
}
