package com.tastyhouse.application.review.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ReviewOwnerReplyDeleteCommand(
    Long ceoId,
    Long shopId,
    Long reviewId
) {
    public ReviewOwnerReplyDeleteCommand {
        if (ceoId == null || shopId == null || reviewId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ReviewOwnerReplyDeleteCommand of(Long ceoId, Long shopId, Long reviewId) {
        return new ReviewOwnerReplyDeleteCommand(ceoId, shopId, reviewId);
    }
}
