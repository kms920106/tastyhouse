package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopBookmarkToggleCommand(
    Long memberId,
    Long shopId
) {

    public ShopBookmarkToggleCommand {
        if (memberId == null || shopId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopBookmarkToggleCommand of(Long memberId, Long shopId) {
        return new ShopBookmarkToggleCommand(memberId, shopId);
    }
}
