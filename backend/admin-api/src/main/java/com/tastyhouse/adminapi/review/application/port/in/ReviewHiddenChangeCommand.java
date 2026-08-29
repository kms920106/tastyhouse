package com.tastyhouse.adminapi.review.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 리뷰 숨김/노출 전환 command. */
public record ReviewHiddenChangeCommand(Long reviewId, Boolean hidden) {
    public ReviewHiddenChangeCommand {
        if (reviewId == null || hidden == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
