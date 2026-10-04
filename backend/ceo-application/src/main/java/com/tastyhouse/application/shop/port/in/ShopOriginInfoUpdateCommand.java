package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopOriginInfoUpdateCommand(
    Long ceoId,
    Long shopId,
    String sourceType,
    String content,
    String url
) {

    public ShopOriginInfoUpdateCommand {
        if (ceoId == null || shopId == null || sourceType == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
