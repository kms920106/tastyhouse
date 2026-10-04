package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ReviewReplyDeleteCommand(Long replyId) {

    public ReviewReplyDeleteCommand {
        if (replyId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ReviewReplyDeleteCommand of(Long replyId) {
        return new ReviewReplyDeleteCommand(replyId);
    }
}
