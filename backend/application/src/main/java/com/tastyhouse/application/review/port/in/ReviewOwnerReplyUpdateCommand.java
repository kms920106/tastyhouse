package com.tastyhouse.application.review.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ReviewOwnerReplyUpdateCommand(
    Long ceoId,
    Long shopId,
    Long reviewId,
    String content
) {
    public ReviewOwnerReplyUpdateCommand {
        if (ceoId == null || shopId == null || reviewId == null || content == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
