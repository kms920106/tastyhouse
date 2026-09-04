package com.tastyhouse.application.review.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 사장님 답변 등록 command.
 */
public record ReviewOwnerReplyCreateCommand(
    Long ceoId,
    Long shopId,
    Long reviewId,
    String content
) {
    public ReviewOwnerReplyCreateCommand {
        if (ceoId == null || shopId == null || reviewId == null || content == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
