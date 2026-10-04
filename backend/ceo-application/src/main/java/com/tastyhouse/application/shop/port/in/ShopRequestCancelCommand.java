package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopRequestCancelCommand(
    Long ceoId,
    Long shopId,
    Long requestId
) {

    public ShopRequestCancelCommand {
        if (ceoId == null || shopId == null || requestId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopRequestCancelCommand of(Long ceoId, Long shopId, Long requestId) {
        return new ShopRequestCancelCommand(ceoId, shopId, requestId);
    }
}
