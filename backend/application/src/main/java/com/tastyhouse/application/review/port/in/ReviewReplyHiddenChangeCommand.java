package com.tastyhouse.application.review.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ReviewReplyHiddenChangeCommand(Long replyId, Boolean hidden) {
    public ReviewReplyHiddenChangeCommand {
        if (replyId == null || hidden == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
