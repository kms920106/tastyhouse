package com.tastyhouse.adminapi.shop.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 주문안내 게시중단 command. */
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
