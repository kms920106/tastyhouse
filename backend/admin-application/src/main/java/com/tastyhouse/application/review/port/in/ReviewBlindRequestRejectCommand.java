package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ReviewBlindRequestRejectCommand(Long requestId, String rejectReason) {

    public ReviewBlindRequestRejectCommand {
        if (requestId == null || rejectReason == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
