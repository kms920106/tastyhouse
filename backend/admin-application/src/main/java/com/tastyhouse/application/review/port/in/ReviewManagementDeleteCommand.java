package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ReviewManagementDeleteCommand(Long reviewId) {

    public ReviewManagementDeleteCommand {
        if (reviewId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ReviewManagementDeleteCommand of(Long reviewId) {
        return new ReviewManagementDeleteCommand(reviewId);
    }
}
