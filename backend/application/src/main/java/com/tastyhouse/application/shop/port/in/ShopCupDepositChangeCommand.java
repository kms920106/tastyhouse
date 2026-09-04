package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 일회용컵 보증금제 대상 사업자 지정/해제 command. */
public record ShopCupDepositChangeCommand(
    Long shopId,
    Boolean enabled
) {
    public ShopCupDepositChangeCommand {
        if (shopId == null || enabled == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
