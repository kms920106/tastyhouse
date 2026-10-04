package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopRiderVisitGuideDeleteCommand(
    Long shopId,
    Long adminId,
    String reason
) {

    public ShopRiderVisitGuideDeleteCommand {
        if (shopId == null || adminId == null || reason == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
