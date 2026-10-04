package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopOrderMethodUnassignCommand(
    Long shopId,
    String orderMethod
) {

    public ShopOrderMethodUnassignCommand {
        if (shopId == null || orderMethod == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopOrderMethodUnassignCommand of(Long shopId, String orderMethod) {
        return new ShopOrderMethodUnassignCommand(shopId, orderMethod);
    }
}
