package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopCeoRevokeCommand(
    Long adminId,
    Long shopId
) {
    public ShopCeoRevokeCommand {
        if (adminId == null || shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopCeoRevokeCommand of(Long adminId, Long shopId) {
        return new ShopCeoRevokeCommand(adminId, shopId);
    }
}
