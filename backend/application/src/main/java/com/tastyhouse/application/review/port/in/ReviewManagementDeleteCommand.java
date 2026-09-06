package com.tastyhouse.application.review.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ReviewManagementDeleteCommand(Long reviewId) {
    public ReviewManagementDeleteCommand {
        if (reviewId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ReviewManagementDeleteCommand of(Long reviewId) {
        return new ReviewManagementDeleteCommand(reviewId);
    }
}
