package com.tastyhouse.application.review.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ReviewReplyDeleteCommand(Long replyId) {
    public ReviewReplyDeleteCommand {
        if (replyId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ReviewReplyDeleteCommand of(Long replyId) {
        return new ReviewReplyDeleteCommand(replyId);
    }
}
