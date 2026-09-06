package com.tastyhouse.application.review.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ReviewCommentCreateCommand(
    Long memberId,
    Long reviewId,
    String content
) {
    public ReviewCommentCreateCommand {
        if (memberId == null || reviewId == null || content == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
