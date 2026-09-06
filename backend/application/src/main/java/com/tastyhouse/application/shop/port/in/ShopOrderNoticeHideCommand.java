package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopOrderNoticeHideCommand(
    Long shopId,
    String reason
) {
    public ShopOrderNoticeHideCommand {
        if (shopId == null || reason == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
