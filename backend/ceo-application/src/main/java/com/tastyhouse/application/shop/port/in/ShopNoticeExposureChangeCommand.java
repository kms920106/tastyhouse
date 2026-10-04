package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopNoticeExposureChangeCommand(
    Long ceoId,
    Long shopId,
    Long noticeId,
    Boolean exposed
) {

    public ShopNoticeExposureChangeCommand {
        if (ceoId == null || shopId == null || noticeId == null || exposed == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
