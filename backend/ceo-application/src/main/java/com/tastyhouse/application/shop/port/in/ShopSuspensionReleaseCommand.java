package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopSuspensionReleaseCommand(
    Long ceoId,
    Long shopId,
    Long suspensionId
) {

    public ShopSuspensionReleaseCommand {
        if (ceoId == null || shopId == null || suspensionId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopSuspensionReleaseCommand of(Long ceoId, Long shopId, Long suspensionId) {
        return new ShopSuspensionReleaseCommand(ceoId, shopId, suspensionId);
    }
}
