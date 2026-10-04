package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ReviewHiddenChangeCommand(Long reviewId, Boolean hidden) {

    public ReviewHiddenChangeCommand {
        if (reviewId == null || hidden == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
