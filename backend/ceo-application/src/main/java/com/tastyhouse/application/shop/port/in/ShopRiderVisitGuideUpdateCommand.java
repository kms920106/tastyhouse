package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopRiderVisitGuideUpdateCommand(
    Long ceoId,
    Long shopId,
    String visitGuide
) {

    public ShopRiderVisitGuideUpdateCommand {
        if (ceoId == null || shopId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
