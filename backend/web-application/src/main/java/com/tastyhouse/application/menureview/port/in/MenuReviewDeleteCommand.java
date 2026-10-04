package com.tastyhouse.application.menureview.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record MenuReviewDeleteCommand(
    Long memberId,
    Long menuReviewId
) {

    public MenuReviewDeleteCommand {
        if (memberId == null || menuReviewId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static MenuReviewDeleteCommand of(Long memberId, Long menuReviewId) {
        return new MenuReviewDeleteCommand(memberId, menuReviewId);
    }
}
