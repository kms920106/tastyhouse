package com.tastyhouse.application.review.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ReviewLikeToggleCommand(
    Long memberId,
    Long reviewId
) {
    public ReviewLikeToggleCommand {
        if (memberId == null || reviewId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ReviewLikeToggleCommand of(Long memberId, Long reviewId) {
        return new ReviewLikeToggleCommand(memberId, reviewId);
    }
}
