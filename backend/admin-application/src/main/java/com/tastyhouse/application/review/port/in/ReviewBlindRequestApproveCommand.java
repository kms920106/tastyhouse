package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ReviewBlindRequestApproveCommand(Long requestId) {

    public ReviewBlindRequestApproveCommand {
        if (requestId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ReviewBlindRequestApproveCommand of(Long requestId) {
        return new ReviewBlindRequestApproveCommand(requestId);
    }
}
