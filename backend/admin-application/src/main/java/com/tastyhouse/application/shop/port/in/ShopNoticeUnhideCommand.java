package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopNoticeUnhideCommand(
    Long adminId,
    Long noticeId
) {

    public ShopNoticeUnhideCommand {
        if (adminId == null || noticeId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopNoticeUnhideCommand of(Long adminId, Long noticeId) {
        return new ShopNoticeUnhideCommand(adminId, noticeId);
    }
}
