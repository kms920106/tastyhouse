package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopOrderMethodAssignCommand(
    Long shopId,
    String orderMethod
) {
    public ShopOrderMethodAssignCommand {
        if (shopId == null || orderMethod == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
