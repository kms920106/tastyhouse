package com.tastyhouse.application.review.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ReviewBlindConsentCommand(
    Long memberId,
    Long reviewId
) {
    public ReviewBlindConsentCommand {
        if (memberId == null || reviewId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ReviewBlindConsentCommand of(Long memberId, Long reviewId) {
        return new ReviewBlindConsentCommand(memberId, reviewId);
    }
}
