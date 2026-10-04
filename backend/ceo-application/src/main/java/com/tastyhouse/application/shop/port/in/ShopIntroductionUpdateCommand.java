package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopIntroductionUpdateCommand(
    Long ceoId,
    Long shopId,
    String message
) {

    public ShopIntroductionUpdateCommand {
        if (ceoId == null || shopId == null || message == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
