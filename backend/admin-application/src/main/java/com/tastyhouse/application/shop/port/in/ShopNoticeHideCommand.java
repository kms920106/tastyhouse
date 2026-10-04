package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopNoticeHideCommand(
    Long adminId,
    Long noticeId,
    String reason
) {

    public ShopNoticeHideCommand {
        if (adminId == null || noticeId == null || reason == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
