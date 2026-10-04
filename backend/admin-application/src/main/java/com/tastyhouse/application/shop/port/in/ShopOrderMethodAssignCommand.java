package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopOrderMethodAssignCommand(
    Long shopId,
    String orderMethod
) {

    public ShopOrderMethodAssignCommand {
        if (shopId == null || orderMethod == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
