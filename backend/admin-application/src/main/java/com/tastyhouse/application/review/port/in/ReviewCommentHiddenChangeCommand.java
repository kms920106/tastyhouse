package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ReviewCommentHiddenChangeCommand(Long commentId, Boolean hidden) {

    public ReviewCommentHiddenChangeCommand {
        if (commentId == null || hidden == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
