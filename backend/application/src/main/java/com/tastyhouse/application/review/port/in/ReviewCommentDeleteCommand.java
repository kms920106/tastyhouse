package com.tastyhouse.application.review.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ReviewCommentDeleteCommand(Long commentId) {
    public ReviewCommentDeleteCommand {
        if (commentId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ReviewCommentDeleteCommand of(Long commentId) {
        return new ReviewCommentDeleteCommand(commentId);
    }
}
