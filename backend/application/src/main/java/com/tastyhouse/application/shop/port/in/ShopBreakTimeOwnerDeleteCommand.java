package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopBreakTimeOwnerDeleteCommand(
    Long ceoId,
    Long breakTimeId
) {
    public ShopBreakTimeOwnerDeleteCommand {
        if (ceoId == null || breakTimeId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopBreakTimeOwnerDeleteCommand of(Long ceoId, Long breakTimeId) {
        return new ShopBreakTimeOwnerDeleteCommand(ceoId, breakTimeId);
    }
}
