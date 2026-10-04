package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopStatusUpdateCommand(
    Long ceoId,
    Long shopId,
    String status
) {

    public ShopStatusUpdateCommand {
        if (ceoId == null || shopId == null || status == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
