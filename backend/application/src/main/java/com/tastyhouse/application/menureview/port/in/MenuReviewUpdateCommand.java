package com.tastyhouse.application.menureview.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record MenuReviewUpdateCommand(
    Long memberId,
    Long menuReviewId,
    Integer rating,
    String comment
) {
    public MenuReviewUpdateCommand {
        if (memberId == null || menuReviewId == null || rating == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
