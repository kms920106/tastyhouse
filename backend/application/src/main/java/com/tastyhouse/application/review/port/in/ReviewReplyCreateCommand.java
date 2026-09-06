package com.tastyhouse.application.review.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ReviewReplyCreateCommand(
    Long memberId,
    Long commentId,
    Long replyToMemberId,
    String content
) {
    public ReviewReplyCreateCommand {
        if (memberId == null || commentId == null || content == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
