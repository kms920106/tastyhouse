package com.tastyhouse.adminapi.shop.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 가게 주문수단 지정 command. {@code orderMethod}는 서비스가 enum으로 승격한다. */
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
