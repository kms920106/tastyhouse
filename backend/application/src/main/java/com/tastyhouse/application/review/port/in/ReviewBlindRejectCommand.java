package com.tastyhouse.application.review.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ReviewBlindRejectCommand(
    Long memberId,
    Long reviewId
) {
    public ReviewBlindRejectCommand {
        if (memberId == null || reviewId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ReviewBlindRejectCommand of(Long memberId, Long reviewId) {
        return new ReviewBlindRejectCommand(memberId, reviewId);
    }
}
