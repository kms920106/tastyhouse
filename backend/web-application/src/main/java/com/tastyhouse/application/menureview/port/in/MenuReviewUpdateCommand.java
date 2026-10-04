package com.tastyhouse.application.menureview.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record MenuReviewUpdateCommand(
    Long memberId,
    Long menuReviewId,
    Integer rating,
    String comment
) {

    public MenuReviewUpdateCommand {
        if (memberId == null || menuReviewId == null || rating == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
