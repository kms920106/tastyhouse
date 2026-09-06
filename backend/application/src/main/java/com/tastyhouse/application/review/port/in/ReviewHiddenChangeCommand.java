package com.tastyhouse.application.review.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ReviewHiddenChangeCommand(Long reviewId, Boolean hidden) {
    public ReviewHiddenChangeCommand {
        if (reviewId == null || hidden == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
