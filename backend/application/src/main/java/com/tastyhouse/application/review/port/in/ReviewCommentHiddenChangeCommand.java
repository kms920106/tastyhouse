package com.tastyhouse.application.review.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ReviewCommentHiddenChangeCommand(Long commentId, Boolean hidden) {
    public ReviewCommentHiddenChangeCommand {
        if (commentId == null || hidden == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
