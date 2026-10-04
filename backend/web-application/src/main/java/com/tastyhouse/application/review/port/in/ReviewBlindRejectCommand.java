package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ReviewBlindRejectCommand(
    Long memberId,
    Long reviewId
) {

    public ReviewBlindRejectCommand {
        if (memberId == null || reviewId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ReviewBlindRejectCommand of(Long memberId, Long reviewId) {
        return new ReviewBlindRejectCommand(memberId, reviewId);
    }
}
