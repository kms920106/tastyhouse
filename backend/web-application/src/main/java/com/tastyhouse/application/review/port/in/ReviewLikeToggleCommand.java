package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ReviewLikeToggleCommand(
    Long memberId,
    Long reviewId
) {

    public ReviewLikeToggleCommand {
        if (memberId == null || reviewId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ReviewLikeToggleCommand of(Long memberId, Long reviewId) {
        return new ReviewLikeToggleCommand(memberId, reviewId);
    }
}
