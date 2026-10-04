package com.tastyhouse.application.menureview.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record MenuReviewCreateCommand(
    Long memberId,
    Long orderProductId,
    Integer rating,
    String comment
) {

    public MenuReviewCreateCommand {
        if (memberId == null || orderProductId == null || rating == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
