package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopOrderMethodUnassignCommand(
    Long shopId,
    String orderMethod
) {
    public ShopOrderMethodUnassignCommand {
        if (shopId == null || orderMethod == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopOrderMethodUnassignCommand of(Long shopId, String orderMethod) {
        return new ShopOrderMethodUnassignCommand(shopId, orderMethod);
    }
}
