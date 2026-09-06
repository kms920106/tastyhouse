package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopBookmarkToggleCommand(
    Long memberId,
    Long shopId
) {
    public ShopBookmarkToggleCommand {
        if (memberId == null || shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopBookmarkToggleCommand of(Long memberId, Long shopId) {
        return new ShopBookmarkToggleCommand(memberId, shopId);
    }
}
