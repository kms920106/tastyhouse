package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopRequestCancelCommand(
    Long ceoId,
    Long shopId,
    Long requestId
) {
    public ShopRequestCancelCommand {
        if (ceoId == null || shopId == null || requestId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopRequestCancelCommand of(Long ceoId, Long shopId, Long requestId) {
        return new ShopRequestCancelCommand(ceoId, shopId, requestId);
    }
}
