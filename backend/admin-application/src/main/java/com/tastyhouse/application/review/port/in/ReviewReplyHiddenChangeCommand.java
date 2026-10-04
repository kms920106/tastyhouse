package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ReviewReplyHiddenChangeCommand(Long replyId, Boolean hidden) {

    public ReviewReplyHiddenChangeCommand {
        if (replyId == null || hidden == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
