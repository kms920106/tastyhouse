package com.tastyhouse.ceoapplication.review.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 사장님 답변 수정 command.
 */
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
