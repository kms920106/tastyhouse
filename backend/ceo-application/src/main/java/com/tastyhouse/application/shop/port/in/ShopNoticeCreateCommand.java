package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopNoticeCreateCommand(
    Long ceoId,
    Long shopId,
    String content,
    Boolean exposed
) {

    public ShopNoticeCreateCommand {
        if (ceoId == null || shopId == null || content == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
