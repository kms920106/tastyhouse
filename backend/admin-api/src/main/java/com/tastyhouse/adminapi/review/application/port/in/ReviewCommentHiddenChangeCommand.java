package com.tastyhouse.adminapi.review.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 리뷰 댓글 숨김/노출 전환 command. */
public record ReviewCommentHiddenChangeCommand(Long commentId, Boolean hidden) {
    public ReviewCommentHiddenChangeCommand {
        if (commentId == null || hidden == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
