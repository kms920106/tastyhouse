package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopNoticeDeleteCommand(
    Long ceoId,
    Long shopId,
    Long noticeId
) {

    public ShopNoticeDeleteCommand {
        if (ceoId == null || shopId == null || noticeId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopNoticeDeleteCommand of(Long ceoId, Long shopId, Long noticeId) {
        return new ShopNoticeDeleteCommand(ceoId, shopId, noticeId);
    }
}
