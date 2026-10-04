package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductImageChangeRejectCommand(Long requestId, String rejectReason) {

    public ProductImageChangeRejectCommand {
        if (requestId == null || rejectReason == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
