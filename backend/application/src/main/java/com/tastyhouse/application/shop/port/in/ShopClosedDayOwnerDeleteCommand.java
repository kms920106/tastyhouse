package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopClosedDayOwnerDeleteCommand(
    Long ceoId,
    Long closedDayId
) {
    public ShopClosedDayOwnerDeleteCommand {
        if (ceoId == null || closedDayId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopClosedDayOwnerDeleteCommand of(Long ceoId, Long closedDayId) {
        return new ShopClosedDayOwnerDeleteCommand(ceoId, closedDayId);
    }
}
