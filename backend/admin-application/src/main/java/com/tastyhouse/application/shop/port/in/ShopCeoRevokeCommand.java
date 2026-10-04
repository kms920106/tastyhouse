package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopCeoRevokeCommand(
    Long adminId,
    Long shopId
) {

    public ShopCeoRevokeCommand {
        if (adminId == null || shopId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopCeoRevokeCommand of(Long adminId, Long shopId) {
        return new ShopCeoRevokeCommand(adminId, shopId);
    }
}
