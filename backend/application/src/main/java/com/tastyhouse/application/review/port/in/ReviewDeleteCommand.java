package com.tastyhouse.application.review.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ReviewDeleteCommand(
    Long memberId,
    Long reviewId
) {
    public ReviewDeleteCommand {
        if (memberId == null || reviewId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ReviewDeleteCommand of(Long memberId, Long reviewId) {
        return new ReviewDeleteCommand(memberId, reviewId);
    }
}
