package com.tastyhouse.application.menureview.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record MenuReviewCreateCommand(
    Long memberId,
    Long orderProductId,
    Integer rating,
    String comment
) {
    public MenuReviewCreateCommand {
        if (memberId == null || orderProductId == null || rating == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
