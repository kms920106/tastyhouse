package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ReviewOwnerReplyDeleteCommand(
    Long ceoId,
    Long shopId,
    Long reviewId
) {

    public ReviewOwnerReplyDeleteCommand {
        if (ceoId == null || shopId == null || reviewId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ReviewOwnerReplyDeleteCommand of(Long ceoId, Long shopId, Long reviewId) {
        return new ReviewOwnerReplyDeleteCommand(ceoId, shopId, reviewId);
    }
}
