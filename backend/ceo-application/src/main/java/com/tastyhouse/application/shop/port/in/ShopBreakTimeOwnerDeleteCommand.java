package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopBreakTimeOwnerDeleteCommand(
    Long ceoId,
    Long breakTimeId
) {

    public ShopBreakTimeOwnerDeleteCommand {
        if (ceoId == null || breakTimeId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopBreakTimeOwnerDeleteCommand of(Long ceoId, Long breakTimeId) {
        return new ShopBreakTimeOwnerDeleteCommand(ceoId, breakTimeId);
    }
}
