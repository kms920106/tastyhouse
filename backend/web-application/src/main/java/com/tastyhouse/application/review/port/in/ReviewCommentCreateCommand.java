package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ReviewCommentCreateCommand(
    Long memberId,
    Long reviewId,
    String content
) {

    public ReviewCommentCreateCommand {
        if (memberId == null || reviewId == null || content == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
