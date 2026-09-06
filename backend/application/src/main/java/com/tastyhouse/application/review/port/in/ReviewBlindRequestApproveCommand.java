package com.tastyhouse.application.review.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ReviewBlindRequestApproveCommand(Long requestId) {
    public ReviewBlindRequestApproveCommand {
        if (requestId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ReviewBlindRequestApproveCommand of(Long requestId) {
        return new ReviewBlindRequestApproveCommand(requestId);
    }
}
