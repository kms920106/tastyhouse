package com.tastyhouse.application.review.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 리뷰 답글 숨김/노출 전환 command. */
public record ReviewReplyHiddenChangeCommand(Long replyId, Boolean hidden) {
    public ReviewReplyHiddenChangeCommand {
        if (replyId == null || hidden == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
