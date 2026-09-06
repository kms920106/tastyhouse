package com.tastyhouse.application.menureview.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record MenuReviewDeleteCommand(
    Long memberId,
    Long menuReviewId
) {
    public MenuReviewDeleteCommand {
        if (memberId == null || menuReviewId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static MenuReviewDeleteCommand of(Long memberId, Long menuReviewId) {
        return new MenuReviewDeleteCommand(memberId, menuReviewId);
    }
}
