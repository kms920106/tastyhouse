package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopSuspensionReleaseCommand(
    Long ceoId,
    Long shopId,
    Long suspensionId
) {
    public ShopSuspensionReleaseCommand {
        if (ceoId == null || shopId == null || suspensionId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopSuspensionReleaseCommand of(Long ceoId, Long shopId, Long suspensionId) {
        return new ShopSuspensionReleaseCommand(ceoId, shopId, suspensionId);
    }
}
