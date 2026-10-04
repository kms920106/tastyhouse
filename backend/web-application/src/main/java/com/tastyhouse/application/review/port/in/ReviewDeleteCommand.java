package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ReviewDeleteCommand(
    Long memberId,
    Long reviewId
) {

    public ReviewDeleteCommand {
        if (memberId == null || reviewId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ReviewDeleteCommand of(Long memberId, Long reviewId) {
        return new ReviewDeleteCommand(memberId, reviewId);
    }
}
