package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ReviewReplyCreateCommand(
    Long memberId,
    Long commentId,
    Long replyToMemberId,
    String content
) {

    public ReviewReplyCreateCommand {
        if (memberId == null || commentId == null || content == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
