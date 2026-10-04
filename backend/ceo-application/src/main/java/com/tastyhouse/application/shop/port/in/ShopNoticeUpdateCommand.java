package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopNoticeUpdateCommand(
    Long ceoId,
    Long shopId,
    Long noticeId,
    String content,
    Boolean keepExistingImages
) {

    public ShopNoticeUpdateCommand {
        if (ceoId == null || shopId == null || noticeId == null || content == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
