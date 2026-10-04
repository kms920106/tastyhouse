package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ReviewOwnerReplyCreateCommand(
    Long ceoId,
    Long shopId,
    Long reviewId,
    String content
) {

    public ReviewOwnerReplyCreateCommand {
        if (ceoId == null || shopId == null || reviewId == null || content == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
